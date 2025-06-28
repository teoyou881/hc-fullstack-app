import React, {useCallback, useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {
  Alert, Button, Col, Container, Form, Image, Modal, Row, Table,
} from 'react-bootstrap';

import skuService from '../../../services/admin/SkuService.js';

// productService는 더 이상 상품명 조회를 위해 사용하지 않으므로 임포트 제거

function AdminSkuListPage() {
  const {productId} = useParams(); // URL에서 productId 추출
  const navigate = useNavigate();

  // const [product, setProduct] = useState(null); // 제거됨
  const [skus, setSkus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // SKU 모달 상태
  const [showSkuModal, setShowSkuModal] = useState(false);
  const [currentSku, setCurrentSku] = useState(null); // 수정할 SKU 데이터 (null이면 새 SKU)

  // SKU Form 상태
  const [skuFormData, setSkuFormData] = useState({
    skuCode      :'',
    price        :'',
    stockQuantity:'',
    // 옵션 그룹 및 옵션 값 필드는 백엔드 DTO에 따라 동적으로 추가될 수 있습니다.
    // 예: optionValue1: '', optionValue2: ''
  });

  // 상품명과 SKU 목록을 불러오는 함수
  const fetchSkus = useCallback(async () => { // 함수 이름 변경: fetchProductAndSkus -> fetchSkus
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      // 1. 상품 상세 정보 불러오기 (상품명을 표시하기 위함) - 제거됨
      // const productResponse = await productService.getProducts(productId);
      // setProduct(productResponse);

      // 2. 해당 상품의 SKU 목록 불러오기
      const skusResponse = await skuService.getSkusByProductId(productId);
      // 백엔드에서 Page<SkuEntity>를 반환한다면 .content 접근, 아니라면 바로 사용
      setSkus(
          Array.isArray(skusResponse) ? skusResponse : skusResponse.content);

    } catch (error) {
      console.error('Error fetching SKUs:', error); // 로그 메시지 수정
      setErrorMessage('SKU 목록을 불러오는데 실패했습니다: ' +
          (error.response?.data?.message || error.message || '알 수 없는 오류')); // 에러 메시지 수정
    } finally {
      setLoading(false);
    }
  }, [productId]); // productId가 변경될 때마다 함수 재생성

  useEffect(() => {
    fetchSkus(); // 함수 호출 이름 변경
    console.log('AdminSkuListPage.jsx: 47', skus);
  }, [fetchSkus]); // fetchSkus가 변경될 때마다 실행

  // SKU 추가/수정 모달 열기
  const handleShowSkuModal = (sku = null) => {
    setCurrentSku(sku); // 수정 모드면 SKU 데이터 설정, 아니면 null
    setSkuFormData(sku ? { // SKU 데이터가 있으면 폼 초기화, 없으면 빈 값으로 초기화
      skuCode      :sku.skuCode || '',
      price        :sku.price || '',
      stockQuantity:sku.stockQuantity || '',
      // 옵션 값도 여기에 로드해야 합니다.
      // 예: optionValue1: sku.optionValues?.[0]?.value || '',
      //     optionValue2: sku.optionValues?.[1]?.value || '',
    } : {
      skuCode      :'',
      price        :'',
      stockQuantity:'',
    });
    setShowSkuModal(true);
  };

  // SKU 모달 닫기
  const handleCloseSkuModal = () => {
    setShowSkuModal(false);
    setCurrentSku(null);
    setSkuFormData({
      skuCode      :'',
      price        :'',
      stockQuantity:'',
    });
    setErrorMessage(''); // 모달 닫을 때 에러 메시지 초기화
  };

  // SKU 폼 데이터 변경 핸들러
  const handleSkuFormChange = (e) => {
    const {name, value} = e.target;
    setSkuFormData(prevState => ({
      ...prevState,
      [name]:value,
    }));
  };

  // SKU 저장 (추가 또는 수정) 핸들러
  const handleSaveSku = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      if (currentSku) {
        // SKU 수정
        await skuService.updateSku(productId, currentSku.id, skuFormData);
        setSuccessMessage('SKU가 성공적으로 수정되었습니다.');
      } else {
        // SKU 추가
        await skuService.createSku(productId, skuFormData);
        setSuccessMessage('SKU가 성공적으로 추가되었습니다.');
      }
      fetchSkus(); // 목록 새로고침 (함수 호출 이름 변경)
      handleCloseSkuModal(); // 모달 닫기
    } catch (error) {
      console.error('Error saving SKU:', error);
      setErrorMessage('SKU 저장에 실패했습니다: ' +
          (error.response?.data?.message || error.message || '알 수 없는 오류'));
    } finally {
      setLoading(false);
    }
  };

  // SKU 삭제 핸들러
  const handleDeleteSku = async (skuId) => {
    if (window.confirm('정말로 이 SKU를 삭제하시겠습니까?')) {
      setLoading(true);
      setErrorMessage('');
      setSuccessMessage('');
      try {
        await skuService.deleteSku(productId, skuId);
        setSuccessMessage('SKU가 성공적으로 삭제되었습니다.');
        fetchSkus(); // 목록 새로고침 (함수 호출 이름 변경)
      } catch (error) {
        console.error('Error deleting SKU:', error);
        setErrorMessage('SKU 삭제에 실패했습니다: ' +
            (error.response?.data?.message || error.message || '알 수 없는 오류'));
      } finally {
        setLoading(false);
      }
    }
  };

  // 첫 번째 SKU에서 상품명 추출 (SKU 목록이 로드된 후에만 시도)
  const productNameFromSku = skus.length > 0
      ? skus[0].skuCode.split(' _')[0]
      : '상품';

  if (loading) {
    return (
        <Container className="my-5 text-center">
          <p>SKU 목록을 불러오는 중...</p>
        </Container>
    );
  }

  if (errorMessage && !successMessage) { // 에러 메시지가 있고 성공 메시지가 없을 때만 표시
    return (
        <Container className="my-5">
          <Alert variant="danger">{errorMessage}</Alert>
          <Button variant="secondary"
                  onClick={() => navigate('/admin/products')}>
            상품 목록으로 돌아가기
          </Button>
        </Container>
    );
  }

  return (
      <Container className="my-5">
        <h1 className="mb-4">
          {`${productNameFromSku}`} SKU 관리 {/* 수정된 부분 */}
        </h1>
        <p className="text-muted">상품 ID: {productId}</p>

        {/* 성공/에러 메시지 */}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}
        {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

        {/*todo*/}
        {/* SKU 추가 버튼 */}
        <Row className="mb-3">
          <Col className="text-end">
            <Button disabled variant="primary"
                    onClick={() => handleShowSkuModal()}>
              + 새 SKU 추가
            </Button>
          </Col>
        </Row>

        {/* SKU 목록 테이블 */}
        {skus.length === 0 ? (
            <Alert variant="info" className="text-center">이 상품에는 등록된 SKU가
                                                          없습니다.</Alert>
        ) : (
            <Table striped bordered hover responsive className="mb-4">
              <thead>
              <tr>
                <th>SKU 코드</th>
                <th>썸네일</th>
                <th>이미지 업로드</th>
                <th>가격</th>
                <th>재고</th>
                <th>관리</th>
              </tr>
              </thead>
              <tbody>
              {skus.map((sku) => (
                  <tr key={sku.id}>
                    <td>{sku.skuCode}</td>
                    <td>
                      {sku.thumbnailUrl?
                          <Image src={sku.thumbnailUrl} alt={`${sku.name} thumbnail`} thumbnail style={{ width: '50px', height: '50px', objectFit: 'cover' }} />:
                          null
                      }

                    </td>
                    <td>
                  <Link to={`/admin/sku/${sku.id}`}
                    className="text-decoration-none">
                      <Button
                          variant="link"
                          size="sm"
                          className="me-2">
                        이미지 업로드
                      </Button>
                   </Link>
                    </td>
                    <td>₩{sku.price.toLocaleString()}</td>
                    <td>{sku.stock}개</td>
                    {/* 여기에 SKU 옵션 값 표시 (예: <td>{sku.optionValues?.[0]?.value}</td>) */}
                    <td>
                      <Button
                          variant="warning"
                          size="sm"
                          className="me-2"
                          onClick={() => handleShowSkuModal(sku)}
                      >
                        수정
                      </Button>
                      <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDeleteSku(sku.id)}
                      >
                        삭제
                      </Button>
                    </td>
                  </tr>
              ))}
              </tbody>
            </Table>
        )}

        <Button variant="secondary" onClick={() => navigate('/admin/products')}>
          상품 목록으로 돌아가기
        </Button>

        {/* SKU 추가/수정 모달 */}
        <Modal show={showSkuModal} onHide={handleCloseSkuModal}>
          <Modal.Header closeButton>
            <Modal.Title>{currentSku ? 'SKU 수정' : '새 SKU 추가'}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form onSubmit={handleSaveSku}>
              <Form.Group className="mb-3">
                <Form.Label>SKU 코드</Form.Label>
                <Form.Control
                    type="text"
                    name="skuCode"
                    value={skuFormData.skuCode}
                    onChange={handleSkuFormChange}
                    placeholder="SKU 코드를 입력하세요"
                    required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>가격</Form.Label>
                <Form.Control
                    type="number"
                    name="price"
                    value={skuFormData.price}
                    onChange={handleSkuFormChange}
                    placeholder="가격을 입력하세요"
                    required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>재고 수량</Form.Label>
                <Form.Control
                    type="number"
                    name="stockQuantity"
                    value={skuFormData.stockQuantity}
                    onChange={handleSkuFormChange}
                    placeholder="재고 수량을 입력하세요"
                    required
                />
              </Form.Group>
              {/* 여기에 상품 옵션 그룹에 따른 동적 SKU 옵션 필드 추가 */}
              {/* 예: <Form.Group className="mb-3"><Form.Label>색상</Form.Label><Form.Control type="text" name="optionValue1" value={skuFormData.optionValue1} onChange={handleSkuFormChange} /></Form.Group> */}
              <Modal.Footer>
                <Button variant="secondary" onClick={handleCloseSkuModal}>
                  취소
                </Button>
                <Button variant="primary" type="submit">
                  {currentSku ? '수정' : '추가'}
                </Button>
              </Modal.Footer>
            </Form>
          </Modal.Body>
        </Modal>
      </Container>
  );
}

export default AdminSkuListPage;