// src/pages/admin/options/AdminOptionTypeList.jsx
import React, { useState, useEffect } from 'react';
import { Table, Button, Form, Modal, Alert } from 'react-bootstrap';
// 옵션 타입 관련 API 서비스를 가정
import optionTypeService from '../../../services/admin/optionTypeService.js'; // 실제 경로에 맞게 수정해주세요
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';

function AdminOptionTypeList() {
  const [optionTypes, setOptionTypes] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentOptionType, setCurrentOptionType] = useState({ id: null, name: '', displayOrder: 0 });
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    fetchOptionTypes();
  }, []);

  const fetchOptionTypes = async () => {
    try {
      setError(null);
      const response = await optionTypeService.getAllOptionTypes(); // API 호출
      // response.data가 배열임을 가정합니다.
      const fetchedOptionTypes = Array.isArray(response.data) ? response.data : [];
      // displayOrder에 따라 정렬하여 표시
      setOptionTypes(fetchedOptionTypes.sort((a, b) => a.displayOrder - b.displayOrder));
    } catch (err) {
      console.error('Failed to fetch option types:', err);
      setError('옵션 타입 목록을 불러오는데 실패했습니다.');
      if (err.response && err.response.status === 403) {
        setError('접근 권한이 없습니다. 관리자만 접근할 수 있습니다.');
      }
    }
  };

  const handleShowCreateModal = () => {
    setIsEditing(false);
    setCurrentOptionType({ id: null, name: '', displayOrder: 0 }); // 초기화
    setError(null);
    setShowModal(true);
  };

  const handleShowEditModal = (optionType) => {
    setIsEditing(true);
    setCurrentOptionType({ ...optionType }); // 기존 데이터 로드
    setError(null);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setError(null);
    setSuccessMessage(null);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCurrentOptionType((prev) => ({
      ...prev,
      [name]: name === 'displayOrder' ? parseInt(value, 10) || 0 : value, // 숫자로 변환
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);

    if (!currentOptionType.name.trim()) {
      setError("옵션 타입 이름은 필수입니다.");
      return;
    }

    try {
      if (isEditing) {
        await optionTypeService.updateOptionType(currentOptionType.id, {
          name: currentOptionType.name,
          displayOrder: currentOptionType.displayOrder,
        });
        setSuccessMessage('옵션 타입이 성공적으로 수정되었습니다.');
      } else {
        await optionTypeService.createOptionType({
          name: currentOptionType.name,
          displayOrder: currentOptionType.displayOrder, // 백엔드에서 자동 부여될 수 있음
        }, optionTypes);
        setSuccessMessage('옵션 타입이 성공적으로 추가되었습니다.');
      }
      fetchOptionTypes(); // 목록 새로고침
      handleCloseModal(); // 모달 닫기
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('정말로 이 옵션 타입을 삭제하시겠습니까? 관련 옵션 값이나 상품 옵션이 있을 경우 삭제되지 않을 수 있습니다.')) {
      try {
        setError(null);
        setSuccessMessage(null);
        await optionTypeService.deleteOptionType(id);
        setSuccessMessage('옵션 타입이 성공적으로 삭제되었습니다.');
        fetchOptionTypes(); // 목록 새로고침
      } catch (err) {
        console.error('Failed to delete option type:', err);
        let errorMessage = '옵션 타입 삭제에 실패했습니다.';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.response && err.response.status === 400 || err.response.status === 409) {
          errorMessage = "이 옵션 타입을 사용하는 옵션 값이나 상품 옵션이 존재하여 삭제할 수 없습니다.";
        }
        setError(errorMessage);
      }
    }
  };

  return (
      <div className="container mt-4">
        <h1>옵션 타입 관리</h1>
        <Button variant="primary" onClick={handleShowCreateModal} className="mb-3">
          <FaPlus className="me-1" /> 새 옵션 타입 추가
        </Button>

        {error && <Alert variant="danger">{error}</Alert>}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}

        <Table striped bordered hover className="option-type-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>이름</th>
            <th>정렬 순서</th>
            <th>생성일</th>
            <th>수정일</th>
            <th>액션</th>
          </tr>
          </thead>
          <tbody>
          {optionTypes.length > 0 ? (
              optionTypes.map((type) => (
                  <tr key={type.id}>
                    <td>{type.id}</td>
                    <td>{type.name}</td>
                    <td>{type.displayOrder}</td>
                    <td>{new Date(type.createdDate).toLocaleDateString()}</td>
                    <td>{new Date(type.lastModifiedDate).toLocaleDateString()}</td>
                    <td>
                      <Button
                          variant="info"
                          size="sm"
                          className="me-1"
                          onClick={() => handleShowEditModal(type)}
                          title="옵션 타입 수정"
                      >
                        <FaEdit />
                      </Button>
                      <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDelete(type.id)}
                          title="옵션 타입 삭제"
                      >
                        <FaTrash />
                      </Button>
                    </td>
                  </tr>
              ))
          ) : (
              <tr>
                <td colSpan="6" className="text-center">
                  등록된 옵션 타입이 없습니다.
                </td>
              </tr>
          )}
          </tbody>
        </Table>

        {/* 옵션 타입 추가/수정 모달 */}
        <Modal show={showModal} onHide={handleCloseModal}>
          <Modal.Header closeButton>
            <Modal.Title>{isEditing ? '옵션 타입 수정' : '새 옵션 타입 추가'}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>이름</Form.Label>
                <Form.Control
                    type="text"
                    name="name"
                    value={currentOptionType.name}
                    onChange={handleChange}
                    required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>정렬 순서</Form.Label>
                <Form.Control
                    type="number"
                    name="displayOrder"
                    value={currentOptionType.displayOrder}
                    onChange={handleChange}
                    min="1"
                />
                <Form.Text className="text-muted">
                  정렬 순서는 옵션 타입의 표시 순서를 결정합니다.
                </Form.Text>
              </Form.Group>
              <Button variant="primary" type="submit">
                {isEditing ? '수정' : '추가'}
              </Button>
              <Button variant="secondary" onClick={handleCloseModal} className="ms-2">
                취소
              </Button>
            </Form>
          </Modal.Body>
        </Modal>
      </div>
  );
}

export default AdminOptionTypeList;