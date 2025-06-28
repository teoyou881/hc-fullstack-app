// src/pages/admin/AdminProductListPage.jsx
import React, {useCallback, useEffect, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {
  Alert, Button, Container, Form, InputGroup, Pagination, Table,
} from 'react-bootstrap'; // React Bootstrap 컴포넌트 임포트
import productService from '../../../services/admin/productService.js';

function AdminProductListPage() {
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // 페이징 상태
  const [page, setPage] = useState(0); // 현재 페이지 (0부터 시작)
  const [size, setSize] = useState(20); // 페이지당 상품 수 (백엔드의 @PageableDefault와 일치시키는 것이 좋음)
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // 검색 상태
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchType, setSearchType] = useState('name'); // 검색 타입 (name, skuCode)

  // 상품 목록을 불러오는 함수
  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      const params = {
        page: page,
        size: size,
        sort: 'createdDate,desc' // 기본 정렬 추가 (백엔드의 @PageableDefault와 일치)
      };
      if (searchKeyword) {
        // 검색 타입에 따라 파라미터 키를 동적으로 설정
        params[searchType] = searchKeyword;
      }

      // productService.js의 getProducts 메서드는 이미 Page 객체를 반환한다고 가정
      const response = await productService.getProducts(params);

      // 받아온 데이터 구조에 맞춰 상태 업데이트
      setProducts(response.content); // 핵심: content 배열을 products 상태에 저장
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
      setPage(response.number); // 백엔드에서 반환된 실제 페이지 번호로 업데이트 (동기화)

    } catch (error) {
      console.error("Error fetching products:", error);
      // 에러 메시지 상세화
      setErrorMessage('상품 목록을 불러오는데 실패했습니다: ' + (error.response?.data?.message || error.message || '알 수 없는 오류'));
    } finally {
      setLoading(false);
    }
  }, [page, size, searchKeyword, searchType]); // page, size, searchKeyword, searchType 변경 시 재실행

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]); // fetchProducts 함수가 변경될 때마다 실행 (의존성 배열에 fetchProducts 추가)

  // 상품 삭제 핸들러
  const handleDeleteProduct = async (productId) => {
    if (window.confirm('정말로 이 상품을 삭제하시겠습니까? 관련 SKU, 옵션 그룹 등이 모두 삭제됩니다.')) {
      setLoading(true);
      setErrorMessage('');
      setSuccessMessage('');
      try {
        await productService.deleteProduct(productId);
        setSuccessMessage('상품이 성공적으로 삭제되었습니다.');
        // 삭제 후 현재 페이지 상품 목록 다시 불러오기
        // setPage를 0으로 초기화하지 않는 이유는, 만약 현재 페이지의 마지막 항목을 삭제했을 경우
        // 이전 페이지로 자동으로 이동해야 할 수 있기 때문입니다.
        // fetchProducts는 현재 page 상태를 사용하므로, 그대로 호출해도 무방합니다.
        // 하지만 만약 삭제 후 페이지가 완전히 비는 경우를 대비하여 totalElements와 totalPages를 비교하여
        // 페이지를 조정하는 로직이 필요할 수도 있습니다. (여기서는 생략)
        fetchProducts();
      } catch (error) {
        console.error("Error deleting product:", error);
        setErrorMessage('상품 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message || '알 수 없는 오류'));
      } finally {
        setLoading(false);
      }
    }
  };

  // 페이지 변경 핸들러
  const handlePageChange = (newPage) => {
    setPage(newPage); // page 상태 변경 -> useEffect에 의해 fetchProducts 재실행
  };

  // 검색 핸들러
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0); // 검색 시 첫 페이지로 이동 (page 상태 변경 -> useEffect에 의해 fetchProducts 재실행)
  };

  if (loading) {
    return (
        <Container className="my-5 text-center">
          <p>상품 목록을 불러오는 중...</p>
        </Container>
    );
  }

  return (
      <Container className="my-5">
        <h1 className="mb-4">상품 관리 목록</h1>

        {/* 성공/에러 메시지 */}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}
        {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

        {/* 검색 폼 */}
        <Form onSubmit={handleSearchSubmit} className="mb-4">
          <InputGroup>
            <Form.Select value={searchType} onChange={(e) => setSearchType(e.target.value)}>
              <option value="name">상품명</option>
              <option value="skuCode">SKU 코드</option>
            </Form.Select>
            <Form.Control
                type="text"
                placeholder="검색어 입력"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
            />
            <Button type="submit" variant="secondary">검색</Button>
          </InputGroup>
        </Form>

        {/* 상품 목록 테이블 */}
        {products?.length === 0 && !loading ? ( // 로딩 중이 아닐 때만 "상품 없음" 표시
            <Alert variant="info" className="text-center">표시할 상품이 없습니다.</Alert>
        ) : (
            <>
              <Table striped bordered hover responsive className="mb-4">
                <thead>
                <tr>
                  <th>ID</th>
                  <th>상품명</th>
                  <th>카테고리</th>
                  <th>가격 범위</th>
                  <th>생성일</th>
                  <th>수정일</th> {/* 수정일 추가 */}
                  <th>관리</th>
                </tr>
                </thead>
                <tbody>
                {products?.map((product) => (
                    <tr key={product.id}>
                      <td>{product.id}</td>
                      <td>
                        <Link to={`/admin/products/${product.id}/sku`} className="text-decoration-none">
                          {product.name}
                        </Link>
                      </td>
                      <td>{product.categoryName}</td>
                      <td>
                        {/* 가격 범위가 있을 경우에만 표시, toLocaleString()으로 통화 형식 지정 */}
                        {product.minPrice !== undefined && product.minPrice !== null &&
                        product.maxPrice !== undefined && product.maxPrice !== null ?
                            `₩${product.minPrice.toLocaleString()} ~ ₩${product.maxPrice.toLocaleString()}` :
                            '가격 정보 없음'
                        }
                      </td>
                      <td>{product.createdDate ? new Date(product.createdDate).toLocaleDateString() : 'N/A'}</td> {/* createdDate 사용 */}
                      <td>{product.lastModifiedDate ? new Date(product.lastModifiedDate).toLocaleDateString() : 'N/A'}</td> {/* lastModifiedDate 사용 */}
                      <td>
                        <Button
                            variant="warning"
                            size="sm"
                            className="me-2"
                            onClick={() => navigate(`/admin/products/edit/${product.id}`)}
                        >
                          수정
                        </Button>
                        <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDeleteProduct(product.id)}
                        >
                          삭제
                        </Button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </Table>

              {/* 페이징 컨트롤 */}
              <div className="d-flex justify-content-center">
                <Pagination>
                  <Pagination.First onClick={() => handlePageChange(0)} disabled={page === 0} />
                  <Pagination.Prev onClick={() => handlePageChange(page - 1)} disabled={page === 0} />

                  {/* 페이지 번호 목록 */}
                  {totalPages > 0 && [...Array(totalPages).keys()].map(num => (
                      <Pagination.Item
                          key={num} // key는 num (페이지 인덱스) 사용
                          active={num === page}
                          onClick={() => handlePageChange(num)}
                      >
                        {num + 1} {/* 사용자에게 보여주는 페이지 번호는 1부터 시작 */}
                      </Pagination.Item>
                  ))}

                  <Pagination.Next onClick={() => handlePageChange(page + 1)} disabled={page === totalPages - 1} />
                  <Pagination.Last onClick={() => handlePageChange(totalPages - 1)} disabled={page === totalPages - 1} />
                </Pagination>
              </div>
              <div className="text-center mt-2">
                <p>총 {totalElements}개 상품, 현재 {page + 1} / {totalPages} 페이지</p> {/* 현재 페이지 번호 1부터 시작 */}
              </div>
            </>
        )}
      </Container>
  );
}

export default AdminProductListPage;