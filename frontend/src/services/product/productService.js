import apiClient from '../../util/apiClient.js';

const productService = {
  // ----------------------------------------------------
  // Category 관련 API
  // ----------------------------------------------------
  getAllCategories: async () => {
    try {
      const response = await apiClient.get('/category');
      return response.data;
    } catch (error) {
      console.error("Error fetching categories:", error);
      throw error;
    }
  },

  // ----------------------------------------------------
  // Option Group 관련 API
  // ----------------------------------------------------
  getAllAvailableOptionGroups: async () => {
    try {
      const response = await apiClient.get('/options/type');
      return response.data;
    } catch (error) {
      console.error("Error fetching available option groups:", error);
      throw error;
    }
  },

  getOptionValuesByGroupId: async (optionGroupId) => {
    try {
      const response = await apiClient.get(`/options/type/${optionGroupId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching option values for group ${optionGroupId}:`, error);
      throw error;
    }
  },

  // ----------------------------------------------------
  // Product 관련 API
  // ----------------------------------------------------
  createProduct: async (productData) => {
    try {
      // FormData인 경우 인터셉터가 자동으로 Content-Type 처리
      const response = await apiClient.post('/product', productData);
      return response.data;
    } catch (error) {
      console.error("Error creating product:", error);
      throw error;
    }
  },

  getProducts: async () => {
    try {
      const response = await apiClient.get('/product');
      return response.data;
    } catch (error) {
      console.error("Error fetching products:", error);
      throw error;
    }
  },

  getProductById: async(productId) =>{
    const response = await apiClient.get(`/product/${productId}`);
    const uniqueSizes = new Set();
    const uniqueColors = new Set();

    let title = ''; // title 초기화

    if (response.data && response.data.length > 0) {
      // 첫 번째 아이템의 skuCode에서 title 추출
      title = response.data[0].skuCode.split('-')[0];

      response.data.forEach(item => {
        if (item.skuCode) {
          const parts = item.skuCode.split('-');

          if (parts.length >= 3) {
            const size = parts[1];
            const color = parts[2];

            if (size) uniqueSizes.add(size);
            if (color) uniqueColors.add(color);
          }
        }
      });
    }

    // Set을 배열로 변환
    const allSizes = Array.from(uniqueSizes);
    const allColors = Array.from(uniqueColors);

    // 새로운 data 객체 생성 및 반환
    const data= {
      title         :title,
      sizes      :allSizes,
      colors     :allColors,
      skus          :response.data
    };
    return data;
  },

  getProductsByCategory(category) {
    return undefined;
  },
  getProductByName(name) {
    return undefined;
  },
};

export default productService;