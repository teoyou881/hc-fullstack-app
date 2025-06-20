import apiAdmin from '../../util/apiAdmin.js';

// 환경 변수에서 관리자 URL 가져오기
const VITE_ADMIN_URL = import.meta.env.VITE_ADMIN_URL;

const imageService = {
  /**
   * 백엔드에 이미지 파일과 SKU ID, 썸네일 상태, displayOrder를 함께 전송하여 이미지를 업로드합니다.
   * @param {File} file - 업로드할 이미지 파일 객체
   * @param {number} productId - 이미지가 속한 상품의 ID (URL 경로에 사용됨)
   * @param {number} skuId - 이미지가 연결될 SKU의 ID (URL 경로에 사용됨)
   * @param {boolean} isThumbnail - 이 이미지가 썸네일인지 여부
   * @param {number} displayOrder - 이미지의 표시 순서
   * @returns {Promise<Object>} 업로드된 이미지 정보 (예: { id, url, fileName, skuId, isThumbnail, displayOrder })
   */
  uploadImage: async (file, skuId, isThumbnail, displayOrder) => {
    try {
      console.log(`Uploading image: ${file.name}, SKU ID: ${skuId}, Thumbnail: ${isThumbnail}, Display Order: ${displayOrder}`);

      const formData = new FormData();
      formData.append('file', file);
      formData.append('isThumbnail', isThumbnail);
      formData.append('displayOrder', displayOrder); // displayOrder 추가
      formData.append('skuId', skuId);

      // 인터셉터가 자동으로 Content-Type 처리
      const response = await apiAdmin.post('/sku/addImage', formData);

      console.log("ImageService.js: 36", response.data);
      return response.data; // 백엔드에서 반환하는 이미지 정보
    } catch (error) {
      console.error("Error uploading image:", error);
      throw error; // 에러를 호출자에게 다시 던짐
    }
  },

  /**
   * 특정 이미지를 삭제합니다.
   * @param {number} skuId - 이미지가 연결된 SKU의 ID
   * @param {number} imageId - 삭제할 이미지의 ID
   * @returns {Promise<boolean>} 삭제 성공 여부 (true/false)
   */
  deleteImage: async ( skuId, imageId) => {
    try {
      const response = await apiAdmin.delete(`/images/${imageId}`);
      return response.data;
    } catch (error) {
      console.error("Error deleting image:", error);
      throw error;
    }
  },

  /**
   * 특정 SKU에 연결된 이미지 목록을 백엔드에서 가져옵니다.
   * @param {number} productId - 이미지가 속한 상품의 ID
   * @param {number} skuId - 이미지를 조회할 SKU의 ID
   * @returns {Promise<Array<Object>>} 이미지 객체 배열
   */
  getImagesBySkuId: async (productId, skuId) => {
    try {
      console.log(`Fetching images for Product ID: ${productId}, SKU ID: ${skuId}`);
      // 백엔드 API 엔드포인트: GET /api/admin/products/{productId}/skus/{skuId}/images
      const response = await apiAdmin.get(`/products/${productId}/skus/${skuId}/images`);
      return response.data; // 이미지 목록 배열 반환
    } catch (error) {
      console.error(`Error fetching images for Product ID ${productId}, SKU ID ${skuId}:`, error);
      throw error;
    }
  },

  /**
   * 특정 이미지의 썸네일 상태를 업데이트합니다.
   * @param {number} productId - 이미지가 속한 상품의 ID
   * @param {number} skuId - 이미지가 연결된 SKU의 ID
   * @param {number} imageId - 업데이트할 이미지의 ID
   * @param {boolean} isThumbnail - 새로운 썸네일 상태 (true/false)
   * @returns {Promise<Object>} 업데이트 성공 여부 및 이미지 정보
   */
  updateImageThumbnailStatus: async (skuId, imageId) => {
    try {
      console.log(`Updating image ${imageId} for / SKU ${skuId}`);

      const response = await apiAdmin.put(`/sku/${skuId}/updateThumbnail`, {imageId});
      return response.data; // 백엔드에서 반환하는 업데이트 결과
    } catch (error) {
      console.error(`Error updating image ${imageId} thumbnail status:`, error);
      throw error;
    }
  },



};

export default imageService;