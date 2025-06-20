// src/pages/AdminProductCreatePage.jsx

import React, { useState, useEffect } from 'react';
import {
  Container, Row, Col, Form, Button, Alert, Card,
  ListGroup, InputGroup, Dropdown
} from 'react-bootstrap';
import { v4 as uuidv4 } from 'uuid';
import productService from '../../../services/admin/productService.js'; // ⭐ 경로 확인
import { isEqualAsNumber } from '../../../util/util.js'; // ⭐ 경로 확인

const AdminProductCreatePage = () => {
  // 상품 기본 정보 상태
  const [productName, setProductName] = useState('');
  const [price, setPrice] = useState('');
  const [description, setDescription] = useState('');
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  // ⭐ 새로운 상태: 상품 색상 (콤마로 구분된 문자열)
  const [productColors, setProductColors] = useState('');

  // 카테고리 목록 상태
  const [categories, setCategories] = useState([]);

  // 백엔드에서 불러온 사용 가능한 옵션 그룹 목록
  const [availableOptionGroups, setAvailableOptionGroups] = useState([]);
  // 현재 상품에 추가된 옵션 그룹 및 그 하위 옵션 값들
  const [selectedOptionGroups, setSelectedOptionGroups] = useState([]);

  // 로딩 및 에러 상태
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const categoriesData = await productService.getAllCategories();
        const optionGroupsData = await productService.getAllAvailableOptionGroups();

        setCategories(categoriesData);
        setAvailableOptionGroups(optionGroupsData);
      } catch (err) {
        console.error("Failed to fetch initial data:", err);
        setError("초기 데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // 백엔드에서 불러온 옵션 그룹을 상품에 추가하는 핸들러 (Async로 변경)
  const handleAddSelectedOptionGroup = async (groupId) => {
    const groupToAdd = availableOptionGroups.find(group => isEqualAsNumber(group.id, groupId));

    if (groupToAdd && !selectedOptionGroups.some(g => isEqualAsNumber(g.id, groupId))) {
      setLoading(true);
      setError('');
      setSuccessMessage('');
      try {
        const fetchedOptionValues = await productService.getOptionValuesByGroupId(groupId);

        setSelectedOptionGroups(prev => {
          const newGroup = {
            id: groupToAdd.id,
            name: groupToAdd.name,
            displayOrder: groupToAdd.displayOrder,
            optionValues: fetchedOptionValues.map(val => ({
              id: val.id,
              name: val.name,
              extraPrice: val.extraPrice || 0,
              displayOrder: val.displayOrder || 0,
            })).sort((a, b) => a.displayOrder - b.displayOrder)
          };
          return [...prev, newGroup].sort((a, b) => a.displayOrder - b.displayOrder);
        });
        setSuccessMessage(`'${groupToAdd.name}' 옵션 그룹이 추가되었습니다.`);
        setTimeout(() => setSuccessMessage(''), 3000);
      } catch (err) {
        console.error(`Failed to fetch option values for group ${groupId}:`, err);
        setError(`'${groupToAdd.name}' 옵션 그룹의 값을 불러오지 못했습니다.`);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleAddOptionValue = (groupId, valueName, extraPrice) => {
    if (!valueName.trim()) {
      setError("옵션 값 이름을 입력해주세요.");
      return;
    }

    setSelectedOptionGroups(prevGroups => prevGroups.map(group => {
      if (isEqualAsNumber(group.id, groupId)) {
        const newOptionValue = {
          id: uuidv4(),
          name: valueName.trim(),
          extraPrice: Number(extraPrice),
          displayOrder: group.optionValues.length + 1
        };
        return {
          ...group,
          optionValues: [...group.optionValues, newOptionValue]
          .sort((a, b) => a.displayOrder - b.displayOrder)
        };
      }
      return group;
    }));
    setSuccessMessage("옵션 값이 추가되었습니다.");
    setTimeout(() => setSuccessMessage(''), 3000);
  };

  const handleDeleteOptionGroup = (groupIdToDelete) => {
    if (window.confirm('이 옵션 그룹을 삭제하시겠습니까? 관련 옵션 값도 모두 삭제됩니다.')) {
      setSelectedOptionGroups(prevGroups => prevGroups.filter(group => !isEqualAsNumber(group.id, groupIdToDelete)));
      setSuccessMessage("옵션 그룹이 삭제되었습니다.");
      setTimeout(() => setSuccessMessage(''), 3000);
    }
  };

  const handleDeleteOptionValue = (groupId, valueIdToDelete) => {
    if (window.confirm('이 옵션 값을 삭제하시겠까?')) {
      setSelectedOptionGroups(prevGroups => prevGroups.map(group =>
          isEqualAsNumber(group.id, groupId)
              ? { ...group, optionValues: group.optionValues.filter(value => value.id !== valueIdToDelete) }
              : group
      ));
      setSuccessMessage("옵션 값이 삭제되었습니다.");
      setTimeout(() => setSuccessMessage(''), 3000);
    }
  };

  const handleSubmitProduct = async (e) => {
    e.preventDefault();

    if (!productName || !price || !selectedCategoryId) {
      setError("상품명, 가격, 카테고리는 필수 입력 항목입니다.");
      return;
    }
    // ⭐ 상품 색상 입력 검증 및 파싱
    const colorsArray = productColors.split(',').map(color => color.trim()).filter(color => color !== '');
    if (colorsArray.length === 0) {
      setError("상품 색상을 최소 한 가지 이상 입력해야 합니다. (예: white, black, red)");
      return;
    }


    setLoading(true);
    setError('');
    setSuccessMessage('');

    const productData = {
      name: productName,
      price: Number(price),
      description: description,
      categoryId: selectedCategoryId,
      // ⭐ 색상 배열을 productData에 추가
      colors: colorsArray,
      optionGroups: selectedOptionGroups.map(group => ({
        id: group.id,
        name: group.name,
        optionValues: group.optionValues.map(value => ({
          ...(typeof value.id === 'number' ? { id: value.id } : {}),
          name: value.name,
          extraPrice: Number(value.extraPrice),
        }))
      }))
    };
    console.log("AdminProductCreatePage.jsx: productData", productData);

    try {
      const response = await productService.createProduct(productData);
      setSuccessMessage(response.message || "상품이 성공적으로 등록되었습니다!");
      // 폼 초기화
      setProductName('');
      setPrice('');
      setDescription('');
      setSelectedCategoryId('');
      setProductColors(''); // ⭐ 색상 필드 초기화
      setSelectedOptionGroups([]);
    } catch (err) {
      console.error("Failed to create product:", err);
      const errorMessage = err.response?.data?.message || err.message || "상품 등록 중 오류가 발생했습니다.";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const selectedOptionGroupIds = new Set(selectedOptionGroups.map(g => g.id));
  const availableOptionGroupsForDropdown = availableOptionGroups.filter(
      group => !selectedOptionGroupIds.has(group.id)
  );

  return (
      <Container className="my-5">
        <h1 className="text-center mb-4">상품 등록</h1>

        {loading && <Alert variant="info" className="text-center">데이터를 불러오거나 처리 중입니다...</Alert>}
        {error && <Alert variant="danger" className="text-center">{error}</Alert>}
        {successMessage && <Alert variant="success" className="text-center">{successMessage}</Alert>}

        <Form onSubmit={handleSubmitProduct}>
          <Card className="mb-4">
            <Card.Header as="h2">기본 정보</Card.Header>
            <Card.Body>
              <Form.Group className="mb-3">
                <Form.Label htmlFor="productName">상품명</Form.Label>
                <Form.Control
                    type="text"
                    id="productName"
                    value={productName}
                    onChange={(e) => setProductName(e.target.value)}
                    required
                />
              </Form.Group>
              {/* ⭐ 새로운 색상 입력 필드 추가 */}
              <Form.Group className="mb-3">
                <Form.Label htmlFor="productColors">상품 색상 (콤마로 구분, 예: white, black, red)</Form.Label>
                <Form.Control
                    type="text"
                    id="productColors"
                    value={productColors}
                    onChange={(e) => setProductColors(e.target.value)}
                    placeholder="white, black, brown"
                />
              </Form.Group>
              {/* ⭐ 기존 가격 입력 필드는 그대로 둠 */}
              <Form.Group className="mb-3">
                <Form.Label htmlFor="price">가격</Form.Label>
                <InputGroup>
                  <Form.Control
                      type="number"
                      id="price"
                      value={price}
                      onChange={(e) => setPrice(e.target.value)}
                      required
                  />
                  <InputGroup.Text>원</InputGroup.Text>
                </InputGroup>
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label htmlFor="description">설명</Form.Label>
                <Form.Control
                    as="textarea"
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={5}
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label htmlFor="category">카테고리</Form.Label>
                <Form.Select
                    id="category"
                    value={selectedCategoryId}
                    onChange={(e) => setSelectedCategoryId(e.target.value)}
                    required
                >
                  <option value="">카테고리 선택</option>
                  {categories.map(cat => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Card.Body>
          </Card>

          <Card className="mb-4">
            <Card.Header as="h2">상품 옵션 관리</Card.Header>
            <Card.Body>
              <Form.Group className="mb-3">
                <Form.Label>옵션 그룹 추가</Form.Label>
                <Dropdown onSelect={handleAddSelectedOptionGroup}>
                  <Dropdown.Toggle variant="outline-primary" id="dropdown-basic" disabled={availableOptionGroupsForDropdown.length === 0}>
                    {availableOptionGroupsForDropdown.length > 0 ? '옵션 그룹 선택' : '모든 옵션 그룹이 추가됨'}
                  </Dropdown.Toggle>
                  <Dropdown.Menu>
                    {availableOptionGroupsForDropdown.map(group => (
                        <Dropdown.Item key={group.id} eventKey={group.id}>
                          {group.name}
                        </Dropdown.Item>
                    ))}
                  </Dropdown.Menu>
                </Dropdown>
              </Form.Group>

              {selectedOptionGroups.length > 0 && (
                  <div className="mt-4">
                    {selectedOptionGroups.map((group) => (
                        <Card key={group.id} className="mb-3">
                          <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5>{group.name}</h5>
                            <Button
                                variant="warning"
                                size="sm"
                                onClick={() => handleDeleteOptionGroup(group.id)}
                            >
                              그룹 삭제
                            </Button>
                          </Card.Header>
                          <Card.Body>
                            <ListGroup variant="flush" className="mb-3">
                              {group.optionValues.length > 0 ? (
                                  group.optionValues.map((value) => (
                                      <ListGroup.Item key={value.id} className="d-flex justify-content-between align-items-center">
                                        {value.name} (추가 가격: {value.extraPrice.toLocaleString()}원)
                                        <Button
                                            variant="danger"
                                            size="sm"
                                            onClick={() => handleDeleteOptionValue(group.id, value.id)}
                                        >
                                          삭제
                                        </Button>
                                      </ListGroup.Item>
                                  ))
                              ) : (
                                  <ListGroup.Item className="text-muted">옵션 값이 없습니다.</ListGroup.Item>
                              )}
                            </ListGroup>
                            {/* todo 값 추가 */}
                            {/* 이전 주석 처리된 부분은 그대로 유지 */}
                          </Card.Body>
                        </Card>
                    ))}
                  </div>
              )}
            </Card.Body>
          </Card>

          <Button variant="success" type="submit" disabled={loading} className="w-100 mt-4 py-3">
            {loading ? '상품 등록 중...' : '상품 등록'}
          </Button>
        </Form>
      </Container>
  );
};

export default AdminProductCreatePage;