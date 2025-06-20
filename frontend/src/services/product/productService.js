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
    return response.data;
  },

  getProductsByCategory(category) {
    return undefined;
  },
  getProductByName(name) {
    return undefined;
  },
};

export default productService;