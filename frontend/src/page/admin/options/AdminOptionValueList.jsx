// src/pages/admin/options/AdminOptionValueList.jsx
import React, { useState, useEffect } from 'react';
import { Table, Button, Form, Modal, Alert } from 'react-bootstrap';
// 옵션 값 및 옵션 타입 관련 API 서비스를 가정
import optionValueService from '../../../services/admin/optionValueService.js'; // 실제 경로에 맞게 수정해주세요
import optionTypeService from '../../../services/admin/optionTypeService.js'; // 실제 경로에 맞게 수정해주세요
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';

function AdminOptionValueList() {
  const [optionValues, setOptionValues] = useState([]);
  const [optionTypes, setOptionTypes] = useState([]); // 옵션 타입 드롭다운을 위한 데이터
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentOptionValue, setCurrentOptionValue] = useState({ id: null, name: '', displayOrder: 0, optionGroupId: null });
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    fetchOptionValues();
    fetchOptionTypesForDropdown(); // 옵션 타입 드롭다운 데이터 로드
  }, []);

  const fetchOptionValues = async () => {
    try {
      setError(null);
      const response = await optionValueService.getAllOptionValues(); // 모든 옵션 값 불러오기
      const fetchedOptionValues = Array.isArray(response.data) ? response.data : [];
      setOptionValues(fetchedOptionValues.sort((a, b) => a.optionGroupId - b.optionGroupId));
    } catch (err) {
      console.error('Failed to fetch option values:', err);
      setError('옵션 값 목록을 불러오는데 실패했습니다.');
      if (err.response && err.response.status === 403) {
        setError('접근 권한이 없습니다. 관리자만 접근할 수 있습니다.');
      }
    }
  };

  const fetchOptionTypesForDropdown = async () => {
    try {
      const response = await optionTypeService.getAllOptionTypes();
      const fetchedOptionTypes = Array.isArray(response.data) ? response.data : [];
      setOptionTypes(fetchedOptionTypes);
    } catch (err) {
      console.error('Failed to fetch option types for dropdown:', err);
      // 드롭다운 로드 실패는 에러로 처리하지 않고 콘솔에만 기록
    }
  };

  const handleShowCreateModal = () => {
    setIsEditing(false);
    setCurrentOptionValue({ id: null, name: '', displayOrder: 0, optionGroupId: null });
    setError(null);
    setShowModal(true);
  };

  const handleShowEditModal = (optionValue) => {
    setIsEditing(true);
    setCurrentOptionValue({ ...optionValue });
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
    setCurrentOptionValue((prev) => ({
      ...prev,
      // optionGroupId와 displayOrder는 숫자로 변환, 나머지는 그대로
      [name]: (name === 'optionGroupId' || name === 'displayOrder') ? parseInt(value, 10) || 0 : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);

    if (!currentOptionValue.name.trim()) {
      setError("옵션 값 이름은 필수입니다.");
      return;
    }
    if (!currentOptionValue.optionGroupId) {
      setError("옵션 타입은 필수입니다.");
      return;
    }

    try {
      if (isEditing) {
        await optionValueService.updateOptionValue(currentOptionValue.id, {
          name: currentOptionValue.name,
          displayOrder: currentOptionValue.displayOrder,
          optionGroupId: currentOptionValue.optionGroupId,
        });
        setSuccessMessage('옵션 값이 성공적으로 수정되었습니다.');
      } else {
        await optionValueService.createOptionValue({
          name: currentOptionValue.name,
          displayOrder: currentOptionValue.displayOrder,
          optionGroupId: currentOptionValue.optionGroupId,
        });
        setSuccessMessage('옵션 값이 성공적으로 추가되었습니다.');
      }
      fetchOptionValues(); // 목록 새로고침
      handleCloseModal(); // 모달 닫기
    } catch (err) {
      console.error('Failed to save option value:', err);
      let errorMessage = '옵션 값 저장에 실패했습니다.';
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      } else if (err.response && err.response.status === 409) {
        errorMessage = "해당 옵션 타입에 이미 같은 이름의 옵션 값이 존재합니다.";
      }
      setError(errorMessage);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('정말로 이 옵션 값을 삭제하시겠습니까? 관련 상품 옵션이 있을 경우 삭제되지 않을 수 있습니다.')) {
      try {
        setError(null);
        setSuccessMessage(null);
        await optionValueService.deleteOptionValue(id);
        setSuccessMessage('옵션 값이 성공적으로 삭제되었습니다.');
        fetchOptionValues(); // 목록 새로고침
      } catch (err) {
        console.error('Failed to delete option value:', err);
        let errorMessage = '옵션 값 삭제에 실패했습니다.';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.response && err.response.status === 400 || err.response.status === 409) {
          errorMessage = "이 옵션 값을 사용하는 상품 옵션이 존재하여 삭제할 수 없습니다.";
        }
        setError(errorMessage);
      }
    }
  };

  // 옵션 타입 ID를 이름으로 변환하는 헬퍼 함수
  const getOptionTypeName = (optionGroupId) => {
    const type = optionTypes.find(type => type.id === optionGroupId);
    return type ? type.name : '알 수 없음';
  };

  return (
      <div className="container mt-4">
        <h1>옵션 값 관리</h1>
        <Button variant="primary" onClick={handleShowCreateModal} className="mb-3">
          <FaPlus className="me-1" /> 새 옵션 값 추가
        </Button>

        {error && <Alert variant="danger">{error}</Alert>}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}

        <Table striped bordered hover className="option-value-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>옵션 타입</th> {/* 어떤 옵션 타입의 값인지 표시 */}
            <th>이름</th>
            <th>정렬 순서</th>
            <th>생성일</th>
            <th>수정일</th>
            <th>액션</th>
          </tr>
          </thead>
          <tbody>
          {optionValues.length > 0 ? (
              optionValues.map((value) => (
                  <tr key={value.id}>
                    <td>{value.id}</td>
                    <td>{getOptionTypeName(value.optionGroupId)}</td> {/* 옵션 타입 이름 표시 */}
                    <td>{value.name}</td>
                    <td>{value.displayOrder}</td>
                    <td>{new Date(value.createdDate).toLocaleDateString()}</td>
                    <td>{new Date(value.lastModifiedDate).toLocaleDateString()}</td>
                    <td>
                      <Button
                          variant="info"
                          size="sm"
                          className="me-1"
                          onClick={() => handleShowEditModal(value)}
                          title="옵션 값 수정"
                      >
                        <FaEdit />
                      </Button>
                      <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleDelete(value.id)}
                          title="옵션 값 삭제"
                      >
                        <FaTrash />
                      </Button>
                    </td>
                  </tr>
              ))
          ) : (
              <tr>
                <td colSpan="7" className="text-center">
                  등록된 옵션 값이 없습니다.
                </td>
              </tr>
          )}
          </tbody>
        </Table>

        {/* 옵션 값 추가/수정 모달 */}
        <Modal show={showModal} onHide={handleCloseModal}>
          <Modal.Header closeButton>
            <Modal.Title>{isEditing ? '옵션 값 수정' : '새 옵션 값 추가'}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>옵션 타입</Form.Label>
                <Form.Select
                    name="optionGroupId"
                    value={currentOptionValue.optionGroupId || ''} // null일 경우 빈 문자열로
                    onChange={handleChange}
                    required
                    disabled={isEditing} // 수정 모드에서는 옵션 타입 변경 불가 (일반적인 정책)
                >
                  <option value="">-- 옵션 타입을 선택하세요 --</option>
                  {optionTypes.map((type) => (
                      <option key={type.id} value={type.id}>
                        {type.name}
                      </option>
                  ))}
                </Form.Select>
                {isEditing && (
                    <Form.Text className="text-muted">
                      옵션 값의 타입은 수정할 수 없습니다.
                    </Form.Text>
                )}
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>이름</Form.Label>
                <Form.Control
                    type="text"
                    name="name"
                    value={currentOptionValue.name}
                    onChange={handleChange}
                    required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>정렬 순서</Form.Label>
                <Form.Control
                    type="number"
                    name="displayOrder"
                    value={currentOptionValue.displayOrder}
                    onChange={handleChange}
                    min="0"
                />
                <Form.Text className="text-muted">
                  정렬 순서는 옵션 값의 표시 순서를 결정합니다.
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

export default AdminOptionValueList;