import axios from 'axios';

const VITE_ADMIN_URL = import.meta.env.VITE_ADMIN_URL;

const skuService = {
  // 특정 상품의 SKU 목록 조회
  getSkusByProductId: async (productId) => {
    try {
      const response = await axios.get(`${VITE_ADMIN_URL}/product/${productId}/sku`);
      return response.data; // Page<SkuEntity> 객체 또는 List<SkuEntity> 반환 가정
    } catch (error) {
      console.error("Error fetching SKUs by product ID:", error);
      throw error;
    }
  },

  getSkuById:async(skuId)=>{
    try {
      const response = await axios.get(`${VITE_ADMIN_URL}/sku/${skuId}`);
      return response.data; // Page<SkuEntity> 객체 또는 List<SkuEntity> 반환 가정
    } catch (error) {
      console.error("Error fetching SKUs by product ID:", error);
      throw error;
    }
  },

  // SKU 추가
  createSku: async (productId, skuData) => {
    try {
      const response = await axios.post(`${VITE_ADMIN_URL}/${productId}/sku`, skuData);
      return response.data;
    } catch (error) {
      console.error("Error creating SKU:", error);
      throw error;
    }
  },

  // SKU 수정
  updateSku: async (productId, skuId, skuData) => {
    try {
      const response = await axios.put(`${VITE_ADMIN_URL}/${productId}/sku/${skuId}`, skuData);
      return response.data;
    } catch (error) {
      console.error("Error updating SKU:", error);
      throw error;
    }
  },

  // SKU 삭제
  deleteSku: async (productId, skuId) => {
    try {
      await axios.delete(`${VITE_ADMIN_URL}/${productId}/sku/${skuId}`);
      return true;
    } catch (error) {
      console.error("Error deleting SKU:", error);
      throw error;
    }
  }
};

export default skuService;