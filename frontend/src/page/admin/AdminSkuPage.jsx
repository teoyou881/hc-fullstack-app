import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container, Row, Col, Button, Alert, Spinner, Card, Form
} from 'react-bootstrap';
import { FaImage, FaTrash, FaCheckCircle } from 'react-icons/fa';

// dnd-kit 임포트
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import imageService from '../../services/admin/ImageService.js';
import skuService from '../../services/admin/SkuService.js';

// 임시 이미지 URL 생성 함수 (파일 객체에서 미리보기를 위해)
const createObjectURL = (file) => (window.URL ? window.URL.createObjectURL(file) : null);
const revokeObjectURL = (url) => (window.URL ? window.URL.revokeObjectURL(url) : null);

// ----------------------------------------------------------------------
// SortableImageItem 컴포넌트: 각 이미지 아이템을 드래그 가능하게 만듬
// ----------------------------------------------------------------------
function SortableImageItem({ item, index, isUploaded, onSetThumbnail, onRemoveSelectedFile, onDeleteUploadedImage }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: item.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    zIndex: isDragging ? 9999 : 'auto',
    boxShadow: isDragging ? '0 4px 8px rgba(0,0,0,0.2)' : 'none',
  };

  return (
      <Col
          xs={6} md={4} lg={3}
          className="mb-3"
          ref={setNodeRef}
          style={style}
          {...attributes} // attributes는 Col에 유지
      >
        <Card className="h-100 position-relative">
          {item.isThumbnail && (
              <div className="position-absolute top-0 start-0 m-2" style={{ zIndex: 10 }}>
                <span className="badge bg-primary">썸네일</span>
              </div>
          )}
          <Card.Img
              variant="top"
              src={isUploaded ? item.url : item.previewUrl}
              alt={isUploaded ? item.fileName : item.file.name}
              className="img-fluid"
              style={{ height: '150px', objectFit: 'contain', cursor: 'pointer' }}
              onClick={() => onSetThumbnail(item.id, isUploaded)}
          />
          <Card.Body className="d-flex flex-column p-2">
            <Card.Text className="text-truncate small mb-1">
              {isUploaded ? item.fileName : item.file.name}
            </Card.Text>
            <Card.Text className="text-muted small">
              순서: {index + 1}
            </Card.Text>
            {/* * 드래그 핸들 역할을 할 요소에 listeners 적용
             * cursor: grab 스타일을 여기에 추가하여 드래그 가능함을 시각적으로 나타냄
             */}
            <div style={{ position: 'absolute', top: '5px', right: '5px', cursor: 'grab', zIndex: 11 }} {...listeners}>
              <FaImage size="20" color="gray" />
            </div>

            {/* 나머지 버튼들 */}
            {isUploaded ? (
                <Button
                    variant="danger"
                    size="sm"
                    onClick={() => onDeleteUploadedImage(item.id)}
                    className="mt-auto"
                >
                  <FaTrash className="me-1" /> 삭제 (업로드됨)
                </Button>
            ) : (
                <Button
                    variant="danger"
                    size="sm"
                    onClick={() => onRemoveSelectedFile(item.id)}
                    className="mt-auto"
                >
                  <FaTrash className="me-1" /> 삭제 (대기 중)
                </Button>
            )}
          </Card.Body>
        </Card>
      </Col>
  );
}

// ----------------------------------------------------------------------
// AdminSkuPage 컴포넌트
// ----------------------------------------------------------------------
function AdminSkuPage() {
  const { skuId } = useParams(); // URL에서 SKU ID와 Product ID를 가져옴
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [selectedFiles, setSelectedFiles] = useState([]); // 사용자가 선택한 파일 목록 (File 객체)
  const [uploadedImages, setUploadedImages] = useState([]); // 서버에 업로드된 이미지 정보 목록 (URL, id, skuId, isThumbnail 등)
  const [currentSku, setCurrentSku] = useState(null); // 현재 관리할 단일 SKU 정보

  // dnd-kit 센서 설정
  const sensors = useSensors(
      useSensor(PointerSensor),
      useSensor(KeyboardSensor, {
        coordinateGetter: sortableKeyboardCoordinates,
      })
  );

  // 드래그 중인 아이템 ID (DragOverlay에 사용)
  const [activeId, setActiveId] = useState(null);

  // 단일 SKU 정보 및 기존 이미지 목록 불러오기
  const fetchSkuAndImages = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      // 1. 특정 SKU 정보 불러오기 (Product ID도 필요할 수 있음)
      // skuService.getSkuById가 productId도 필요하다면 수정 필요
      // 현재는 skuId만 사용하는 것으로 가정
      const skuResponse = await skuService.getSkuById(skuId); // assuming this fetches product info too
      setCurrentSku(skuResponse);

      // 이미지 URL 필드를 url로 매핑
      const mappedImages = skuResponse.images.map(img => ({
        ...img,
        url: img.imageUrl, // 백엔드에서 imageUrl로 내려온다고 가정
        isThumbnail: img.thumbnail, // 썸네일 정보도 매핑
        // dnd-kit을 위해 고유한 string ID가 필요. 백엔드 ID가 이미 고유하므로 그대로 사용
        id: img.id.toString(),
      }));
      setUploadedImages(mappedImages);

    } catch (error) {
      console.error(`Error fetching SKU ${skuId} or existing images:`, error);
      setErrorMessage(`SKU 정보 또는 기존 이미지를 불러오는데 실패했습니다: ${error.response?.data?.message || error.message || '알 수 없는 오류'}`);
    } finally {
      setLoading(false);
    }
  }, [skuId]); // productId를 fetchSkuAndImages의 의존성 배열에 추가해야 할 수도 있습니다.

  useEffect(() => {
    fetchSkuAndImages();
  }, [fetchSkuAndImages]);

  // 파일 선택 핸들러
  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;

    const newSelectedFiles = files.map(file => ({
      file: file,
      id: `${file.name}-${Date.now()}-${Math.random()}`.replace(/[^a-zA-Z0-9-]/g, ''), // 고유 ID 생성 (React key, dnd-kit id) - 특수문자 제거
      previewUrl: createObjectURL(file),
      isThumbnail: false,
      displayOrder: -1, // 임시 값
    }));

    setSelectedFiles(prev => {
      const combinedFiles = [...prev, ...newSelectedFiles];
      const allDisplayableImages = [...uploadedImages, ...combinedFiles]; // 모든 표시될 이미지 (업로드된 + 선택된)

      // 전체 이미지 중 썸네일이 없으면 첫 번째를 썸네일로 지정 (새로 추가된 파일 우선)
      if (!allDisplayableImages.some(item => item.isThumbnail) && allDisplayableImages.length > 0) {
        if (newSelectedFiles.length > 0) {
          // 새로 선택된 파일 중 첫 번째를 썸네일로 설정
          newSelectedFiles[0].isThumbnail = true;
        } else if (uploadedImages.length > 0) {
          // 새로 선택된 파일이 없고, 기존 업로드된 이미지 중 썸네일이 없으면 첫 번째를 썸네일로
          // 이 로직은 백엔드에서 썸네일을 관리하는 방식에 따라 달라질 수 있음.
          // 여기서는 프론트엔드에서 썸네일이 없을 때 시각적으로 첫 번째를 지정하는 역할만 함.
          // 실제 썸네일은 백엔드 API를 통해 업데이트되어야 함.
        }
      }
      return combinedFiles;
    });

    e.target.value = ''; // 같은 파일을 다시 선택할 수 있도록 input 초기화
  };

  // 썸네일 변경 핸들러
  const handleSetThumbnail = async (imageIdToSet, isUploaded = false) => {
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      // 백엔드 호출 여부를 결정할 정확한 isThumbnailChanged 판단
      let currentImageIsThumbnail = false;
      // 선택된 파일 목록 확인
      const foundInSelected = selectedFiles.find(file => file.id === imageIdToSet);
      if (foundInSelected && foundInSelected.isThumbnail) {
        currentImageIsThumbnail = true;
      }
      // 업로드된 이미지 목록 확인 (선택된 파일에서 찾지 못했을 경우)
      if (!currentImageIsThumbnail) {
        const foundInUploaded = uploadedImages.find(img => img.id === imageIdToSet);
        if (foundInUploaded && foundInUploaded.isThumbnail) {
          currentImageIsThumbnail = true;
        }
      }

      const willBeThumbnailChanged = !currentImageIsThumbnail; // 현재 썸네일이 아니라면 변경될 예정

      // 1. 프론트엔드 상태 업데이트 (선택된 파일 + 업로드된 파일 모두 썸네일 상태 갱신)
      setSelectedFiles(prevFiles =>
          prevFiles.map(file => ({ ...file, isThumbnail: file.id === imageIdToSet }))
      );

      setUploadedImages(prevImages =>
          prevImages.map(img => ({ ...img, isThumbnail: img.id === imageIdToSet }))
      );

      console.log("AdminSkuPage.jsx: 243", imageIdToSet, willBeThumbnailChanged);

      // 2. 백엔드 API 호출 (업로드된 이미지인 경우에만)
      if (isUploaded && willBeThumbnailChanged) { // 미리 계산된 willBeThumbnailChanged 사용
        if (!currentSku || !currentSku.id) {
          throw new Error("SKU ID is missing for thumbnail update.");
        }
        await imageService.updateImageThumbnailStatus(
            currentSku.id,
            imageIdToSet,
        );
        setSuccessMessage('썸네일이 성공적으로 변경되었습니다.');
      } else if (!isUploaded && willBeThumbnailChanged) {
        setSuccessMessage('썸네일이 변경되었습니다. (이 변경은 이미지를 업로드할 때 백엔드에 반영됩니다.)');
      } else {
        setSuccessMessage('이미 해당 이미지가 썸네일입니다.');
      }
    } catch (error) {
      console.error("Error setting thumbnail:", error);
      setErrorMessage('썸네일 변경에 실패했습니다: ' + (error.response?.data?.message || error.message || '알 수 없는 오류'));
    } finally {
      setLoading(false);
    }
  };


  // 이미지 삭제 핸들러 (업로드 전)
  const handleRemoveSelectedFile = (fileIdToRemove) => {
    setSelectedFiles(prevFiles => {
      const newFiles = prevFiles.filter(file => file.id !== fileIdToRemove);
      const allRemainingImages = [...uploadedImages, ...newFiles];

      // 삭제 후 썸네일이 없어졌고, 남은 파일이 있다면 첫 번째를 썸네일로 설정 (선택된 파일 우선)
      if (!allRemainingImages.some(f => f.isThumbnail) && allRemainingImages.length > 0) {
        if (newFiles.length > 0) {
          newFiles[0].isThumbnail = true;
        } else if (uploadedImages.length > 0) {
          // 백엔드에 첫 번째 업로드된 이미지를 썸네일로 설정하는 API 호출 필요
          // handleSetThumbnail(uploadedImages[0].id, true);
        }
      }
      // 미리보기 URL 해제
      const removedFile = prevFiles.find(file => file.id === fileIdToRemove);
      if (removedFile && removedFile.previewUrl) {
        revokeObjectURL(removedFile.previewUrl);
      }
      return newFiles;
    });
  };

  // 이미지 삭제 핸들러 (업로드 후)
  const handleDeleteUploadedImage = async (imageId) => {
    if (window.confirm('정말로 이 이미지를 삭제하시겠습니까?')) {
      setLoading(true);
      setErrorMessage('');
      setSuccessMessage('');
      try {
        if (!currentSku || !currentSku.id) {
          throw new Error("Product ID or SKU ID is missing for image deletion.");
        }
        const data = await imageService.deleteImage(currentSku.id, imageId);
        // setUploadedImages(prevImages => {
        //   const newImages = prevImages.filter(img => img.id !== imageId);
        //   // 삭제 후 썸네일이 없어졌다면, 남아있는 이미지 중 첫 번째를 썸네일로 설정
        //   if (newImages.length > 0 && !newImages.some(img => img.isThumbnail)) {
        //     // 백엔드 썸네일 업데이트 API 호출 (이전 썸네일 삭제 시 새 썸네일 지정)
        //     handleSetThumbnail(newImages[0].id, true); // 새 썸네일을 백엔드에도 반영
        //   }
        //   return newImages;
        // });
        console.log('AdminSkuPage.jsx: 315', data);
        console.log("AdminSkuPage.jsx: 316", uploadedImages);
        setUploadedImages(data.map(img => ({
          ...img,
          url: img.imageUrl, // 백엔드에서 imageUrl로 내려온다고 가정
          isThumbnail: img.thumbnail, // 썸네일 정보도 매핑
          // dnd-kit을 위해 고유한 string ID가 필요. 백엔드 ID가 이미 고유하므로 그대로 사용
          id: img.id.toString(),
        })));
        setSuccessMessage('이미지가 성공적으로 삭제되었습니다.');
      } catch (error) {
        console.error("Error deleting image:", error);
        setErrorMessage('이미지 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message || '알 수 없는 오류'));
      } finally {
        setLoading(false);
      }
    }
  };

  // 모든 이미지 업로드 핸들러
  const handleUploadAllImages = async () => {
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    if (!currentSku || !currentSku.id) {
      setErrorMessage('SKU 정보 또는 상품 ID가 유효하지 않습니다.');
      setLoading(false);
      return;
    }

    if (selectedFiles.length === 0) {
      setErrorMessage('업로드할 이미지를 선택해주세요.');
      setLoading(false);
      return;
    }

    const uploadPromises = selectedFiles.map(async (fileInfo) => {
      try {
        // 이미 업로드된 이미지 수 + 현재 업로드할 이미지의 인덱스를 displayOrder로 사용
        const displayOrder = uploadedImages.length + selectedFiles.indexOf(fileInfo);

        const uploadedImage = await imageService.uploadImage(
            fileInfo.file,
            currentSku.id,
            fileInfo.isThumbnail,
            displayOrder // displayOrder 추가
        );
        revokeObjectURL(fileInfo.previewUrl); // 업로드 후 미리보기 URL 해제
        return { ...uploadedImage, id: uploadedImage.id.toString() }; // dnd-kit을 위해 id를 string으로 변환
      } catch (error) {
        console.error(`Error uploading image ${fileInfo.file.name}:`, error);
        setErrorMessage(prev => prev + `\n${fileInfo.file.name} 업로드 실패: ${error.message || '알 수 없는 오류'}`);
        return null;
      }
    });

    const results = await Promise.all(uploadPromises);
    const successfullyUploaded = results.filter(result => result !== null);

    if (successfullyUploaded.length > 0) {
      setSuccessMessage(`${successfullyUploaded.length}개의 이미지가 성공적으로 업로드되었습니다.`);
      // 새로 업로드된 이미지와 기존 이미지 합치기 및 정렬 (백엔드 displayOrder가 적용된 상태로 가정)
      // dnd-kit으로 재정렬된 순서를 백엔드에 반영하는 로직이 필요
      setUploadedImages(prev => [...prev, ...successfullyUploaded]);
      setSelectedFiles([]); // 업로드 완료된 파일 목록 비우기
      // 다시 불러와서 백엔드 정렬 순서 적용 (선택 사항)
      fetchSkuAndImages();
    } else {
      setErrorMessage('모든 이미지 업로드에 실패했습니다.');
    }
    setLoading(false);
  };

  // dnd-kit 드래그 시작 시 호출
  const handleDragStart = (event) => {
    setActiveId(event.active.id);
  };

  // dnd-kit 드래그 종료 시 호출되는 함수
  const handleDragEnd = async (event) => {
    const { active, over } = event;
    setActiveId(null); // 드래그 오버레이 비활성화

    if (!over || active.id === over.id) {
      return;
    }

    // 선택된 파일 목록 내에서 순서 변경
    const selectedFileIds = selectedFiles.map(file => file.id);
    if (selectedFileIds.includes(active.id) && selectedFileIds.includes(over.id)) {
      setSelectedFiles((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id);
        const newIndex = items.findIndex((item) => item.id === over.id);
        return arrayMove(items, oldIndex, newIndex);
      });
    }
    // 업로드된 이미지 목록 내에서 순서 변경
    else { // Assume it's uploaded images if not selected files
      const uploadedImageIds = uploadedImages.map(img => img.id);
      if (uploadedImageIds.includes(active.id) && uploadedImageIds.includes(over.id)) {
        setUploadedImages((items) => {
          const oldIndex = items.findIndex((item) => item.id === active.id);
          const newIndex = items.findIndex((item) => item.id === over.id);
          const newOrder = arrayMove(items, oldIndex, newIndex);

          // TODO: 백엔드에 displayOrder 업데이트 요청 (업로드된 이미지의 순서 변경)
          // 이 로직은 `newOrder`를 바탕으로 백엔드 API를 호출해야 합니다.
          // 예: await imageService.updateImageOrder(currentSku.productId, currentSku.id, newOrder.map(img => img.id));
          // 성공 메시지 설정 등
          console.log("업로드된 이미지 순서 변경. 백엔드 업데이트 필요:", newOrder.map(img => ({ id: img.id, displayOrder: items.indexOf(img) })));

          return newOrder;
        });
      }
      // 드래그앤드롭이 서로 다른 목록 간에 이루어질 경우 (선택된 파일 -> 업로드된 파일, 또는 그 반대)
      // 이 로직은 현재 구현되지 않았지만, 필요한 경우 여기에 추가할 수 있습니다.
      // (예: 선택된 파일을 업로드된 파일 목록으로 드래그 시 바로 업로드 처리)
    }
  };


  // 컴포넌트 언마운트 시 미리보기 URL 해제
  useEffect(() => {
    return () => {
      selectedFiles.forEach(file => {
        if (file.previewUrl) {
          revokeObjectURL(file.previewUrl);
        }
      });
    };
  }, [selectedFiles]);

  // activeId에 해당하는 파일 객체 찾기 (DragOverlay 렌더링용)
  const activeItem = activeId
      ? [...selectedFiles, ...uploadedImages].find(item => item.id === activeId)
      : null;

  // 로딩 상태 확인
  if (loading && !currentSku) { // 초기 로딩 상태: SKU 정보가 아직 없는 경우
    return (
        <Container className="my-5 text-center">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
          <p className="mt-2">SKU 정보를 불러오는 중...</p>
        </Container>
    );
  }

  // 에러 메시지 (SKU 정보를 불러오지 못한 경우)
  if (errorMessage && !currentSku) {
    return (
        <Container className="my-5">
          <Alert variant="danger">{errorMessage}</Alert>
          <Button variant="secondary" onClick={() => navigate('/admin/products')}>
            상품 목록으로 돌아가기
          </Button>
        </Container>
    );
  }

  // SKU 정보가 없는데 에러 메시지도 없다면 (예: SKU ID가 유효하지 않음)
  if (!currentSku) {
    return (
        <Container className="my-5">
          <Alert variant="danger">유효하지 않은 SKU ID입니다.</Alert>
          <Button variant="secondary" onClick={() => navigate('/admin/products')}>
            상품 목록으로 돌아가기
          </Button>
        </Container>
    );
  }

  return (
      <Container className="my-5">
        <h1 className="mb-4">
          SKU Name: {currentSku.skuCode}
        </h1>
        <p className="text-muted">SKU ID: {skuId}</p>
        <p className="text-muted">SKU Price: ₩{currentSku.price?.toLocaleString()}</p>
        <p className="text-muted">SKU Stock: {currentSku.stock?.toLocaleString()}개</p>


        {/* 메시지 영역 */}
        {successMessage && <Alert variant="success">{successMessage}</Alert>}
        {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}

        {/* 파일 선택 */}
        <Form.Group controlId="formFileMultiple" className="mb-3">
          <Form.Label>이미지 파일 선택 (여러 장 선택 가능)</Form.Label>
          <Form.Control type="file" multiple onChange={handleFileChange} accept="image/*" />
        </Form.Group>

        {/* DndContext로 전체 드래그 앤 드롭 영역 감싸기 */}
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
        >
          {/* 선택된 이미지 미리보기 및 관리 */}
          {selectedFiles.length > 0 && (
              <div className="mb-4 p-3 border rounded">
                <h5>업로드 대기 중인 이미지 ({selectedFiles.length}장) (드래그하여 순서 변경)</h5>
                {/* SortableContext: selectedFiles를 위한 드롭 영역 */}
                <SortableContext
                    items={selectedFiles.map(file => file.id)}
                    strategy={verticalListSortingStrategy}
                >
                  <Row>
                    {selectedFiles.map((fileInfo, index) => (
                        <SortableImageItem
                            key={fileInfo.id}
                            item={fileInfo}
                            index={index}
                            isUploaded={false}
                            onSetThumbnail={handleSetThumbnail}
                            onRemoveSelectedFile={handleRemoveSelectedFile}
                        />
                    ))}
                  </Row>
                </SortableContext>
                <div className="text-end mt-3">
                  <Button
                      variant="success"
                      onClick={handleUploadAllImages}
                      disabled={loading || selectedFiles.length === 0}
                  >
                    {loading ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" /> : '모든 이미지 업로드'}
                  </Button>
                </div>
              </div>
          )}

          {/* 이미 업로드된 이미지 목록 */}
          {uploadedImages.length > 0 && (
              <div className="mb-4 p-3 border rounded">
                <h5>업로드된 이미지 ({uploadedImages.length}장) (드래그하여 순서 변경)</h5>
                {/* SortableContext: uploadedImages를 위한 드롭 영역 */}
                <SortableContext
                    items={uploadedImages.map(img => img.id)}
                    strategy={verticalListSortingStrategy}
                >
                  <Row>
                    {uploadedImages.map((img, index) => (
                        <SortableImageItem
                            key={img.id}
                            item={img}
                            index={index}
                            isUploaded={true}
                            onSetThumbnail={handleSetThumbnail}
                            onDeleteUploadedImage={handleDeleteUploadedImage}
                        />
                    ))}
                  </Row>
                </SortableContext>
              </div>
          )}

          {/* DragOverlay: 드래그 중인 아이템의 시각적 피드백 제공 */}
          <DragOverlay>
            {activeItem ? (
                <Card style={{
                  width: '200px', // 적절한 너비 설정
                  opacity: 0.8,
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                  backgroundColor: 'white',
                  cursor: 'grabbing',
                }}>
                  <Card.Img
                      variant="top"
                      src={activeItem.previewUrl || activeItem.url}
                      alt={activeItem.file?.name || activeItem.fileName}
                      className="img-fluid"
                      style={{ height: '100px', objectFit: 'contain' }}
                  />
                  <Card.Body className="p-2">
                    <Card.Text className="text-truncate small">
                      {activeItem.file?.name || activeItem.fileName}
                    </Card.Text>
                  </Card.Body>
                </Card>
            ) : null}
          </DragOverlay>
        </DndContext>

        <Button variant="secondary" onClick={() => navigate(`/admin/products/${currentSku.productId}/sku`)}>
          SKU 목록으로 돌아가기
        </Button>
      </Container>
  );
}

export default AdminSkuPage;