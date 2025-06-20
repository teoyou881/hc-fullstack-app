// src/services/categoryService.js
import axios from 'axios';

const VITE_ADMIN_CATEGORY_URL = import.meta.env.VITE_ADMIN_URL + '/category';

// 인증 헤더 추가를 위한 헬퍼 함수
const getConfig = () => {
  const token = localStorage.getItem('token'); // 로컬 스토리지에서 토큰 가져오기 (실제로는 Context/Redux에서 관리)
  return {
    headers: {
      Authorization: `Bearer ${token}`, // JWT 토큰을 Authorization 헤더에 포함
      'Content-Type': 'application/json',
    },
  };
};

const categoryService = {
  // 모든 카테고리 조회
  // 실제 데이터 배열을 반환합니다.
  getAllCategories: async () => {
    try {
      const response = await axios.get(VITE_ADMIN_CATEGORY_URL);

      return response.data; // 응답 데이터 반환
    } catch (error) {
      console.error("Error fetching categories:", error);
      throw error; // 에러를 호출자에게 다시 던짐
    }
  },

  // 카테고리 생성
  // 생성된 카테고리 객체를 반환합니다.
  createCategory: async (categoryData) => {
    try {
      const response = await axios.post(VITE_ADMIN_CATEGORY_URL, categoryData);

      return response.data; // 생성된 카테고리 데이터 반환
    } catch (error) {
      console.error("Error creating category:", error);
      throw error;
    }
  },

  // 카테고리 수정
  // 수정된 카테고리 객체를 반환합니다.
  updateCategory: async (id, categoryData) => {
    try {
      const response = await axios.put(`${VITE_ADMIN_CATEGORY_URL}/${id}`, categoryData);
      return response.data; // 수정된 카테고리 데이터 반환
    } catch (error) {
      console.error(`Error updating category with ID ${id}:`, error);
      throw error;
    }
  },

  // 카테고리 삭제
  // 삭제 성공 여부 또는 삭제된 데이터(서버 응답에 따라)를 반환합니다.
  deleteCategory: async (id) => {
    try {
      const response = await axios.delete(`${VITE_ADMIN_CATEGORY_URL}/${id}`);
      // 서버 응답에 따라 달라질 수 있습니다.
      // 204 No Content인 경우 response.data는 비어있을 수 있습니다.
      // 여기서는 성공 상태 코드를 받았으므로 true를 반환하거나 response.data를 반환할 수 있습니다.
      return response.data; // 또는 true; (성공 여부만 필요한 경우)
    } catch (error) {
      console.error(`Error deleting category with ID ${id}:`, error);
      throw error;
    }
  },
};


// const categoryService = {
//   // 모든 카테고리 조회
//   getAllCategories: () => {
//     return axios.get(API_BASE_URL, getConfig());
//   },
//
//   // 카테고리 생성
//   createCategory: (categoryData) => {
//     return axios.post(API_BASE_URL, categoryData, getConfig());
//   },
//
//   // 카테고리 수정
//   updateCategory: (id, categoryData) => {
//     return axios.put(`${API_BASE_URL}/${id}`, categoryData, getConfig());
//   },
//
//   // 카테고리 삭제
//   deleteCategory: (id) => {
//     return axios.delete(`${API_BASE_URL}/${id}`, getConfig());
//   },
// };

export default categoryService;