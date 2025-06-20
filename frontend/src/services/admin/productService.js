import axios from 'axios';
const VITE_ADMIN_PRODUCT_URL = import.meta.env.VITE_ADMIN_URL + '/product';
const VITE_ADMIN_URL = import.meta.env.VITE_ADMIN_URL;

// Axios 인스턴스 생성 (필요에 따라 인증 토큰 등 설정 가능)
const api = axios.create({
  baseURL: VITE_ADMIN_PRODUCT_URL,
  headers: {
    'Content-Type': 'application/json',
    // 'Authorization': `Bearer ${localStorage.getItem('token')}` // 인증 토큰이 있다면 추가
  },
});

const productService = {
  // ----------------------------------------------------
  // Category 관련 API
  // ----------------------------------------------------
  getAllCategories: async () => {
    try {
      const response = await axios.get(VITE_ADMIN_URL+'/category'); // 평면 리스트를 가져오는 API 호출
      return response.data;
    } catch (error) {
      console.error("Error fetching categories:", error);
      throw error; // 에러를 호출자에게 다시 던져서 처리하도록 함
    }
  },

  // ----------------------------------------------------
  // Option Group 관련 API
  // ----------------------------------------------------
  // 백엔드에 미리 등록된 사용 가능한 모든 옵션 그룹을 불러옵니다.
  getAllAvailableOptionGroups: async () => {
    try {
      const response = await axios.get( VITE_ADMIN_URL+'/options/type'); // 백엔드 API 경로에 맞게 수정
      return response.data;
    } catch (error) {
      console.error("Error fetching available option groups:", error);
      throw error;
    }
  },

  // ⭐ 새로 추가될 함수: 특정 옵션 그룹의 옵션 값들을 불러오는 API
  getOptionValuesByGroupId: async (optionGroupId) => {
    try {
      const response = await axios.get(VITE_ADMIN_URL + `/options/type/${optionGroupId}`); // 백엔드 API 경로 확인 필요
      return response.data;
    } catch (error) {
      console.error(`Error fetching option values for group ${optionGroupId}:`, error);
      throw error;
    }
  },

  // ----------------------------------------------------
  // Product 관련 API
  // ----------------------------------------------------
  // 상품 등록 API
  // 이미지 파일이 포함되므로 FormData를 사용합니다.
  createProduct: async (productData) => {
    try {
      // Content-Type은 'multipart/form-data'로 자동 설정됩니다.
      const response = await axios.post(VITE_ADMIN_URL + '/product', productData);
      return response.data;
    } catch (error) {
      console.error("Error creating product:", error);
      throw error;
    }
  },

  getProducts:async ()=>{
    try {
      const response = await axios.get(VITE_ADMIN_URL + '/product');
      console.log("productService.js: 72", response.data);
      return response.data;
    } catch (error) {
        console.error(error);
        throw error;
    }
  }
};

export default productService;