import { useQuery } from '@tanstack/react-query';
import productService from '../../services/product/productService.js'; // productService 경로 확인

/**
 * 모든 상품 목록을 가져오는 훅
 */
export const useAllProducts = () => {
  return useQuery({
    queryKey: ['products', 'all'], // 모든 상품 목록을 위한 쿼리 키
    queryFn: productService.getProducts,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * 특정 ID의 상품 상세 정보를 가져오는 훅
 * @param {string} productId - 가져올 상품의 ID
 */
export const useProductById = (productId) => {
  return useQuery({
    queryKey: ['product', productId], // 특정 상품 ID를 포함하는 쿼리 키
    queryFn: () => productService.getProductById(productId), // productId를 인자로 받는 함수
    enabled: !!productId, // productId가 있을 때만 쿼리 실행
    staleTime: 5 * 60 * 1000,
    gcTime:1000*60*10,
    // onError, onSuccess 등 추가 옵션 가능
  });
};

/**
 * 특정 카테고리의 상품 목록을 가져오는 훅
 * @param {string} category - 가져올 상품의 카테고리
 */
export const useProductsByCategory = (category) => {
  return useQuery({
    queryKey: ['products', 'category', category], // 카테고리를 포함하는 쿼리 키
    queryFn: () => productService.getProductsByCategory(category), // category를 인자로 받는 함수
    enabled: !!category, // category가 있을 때만 쿼리 실행
    staleTime: 5 * 60 * 1000,
  });
};

export const useProductByName=(name)=>{
  return useQuery({
    queryKey: ['product', name], // 특정 상품 ID를 포함하는 쿼리 키
    queryFn: () => productService.getProductByName(name), // productId를 인자로 받는 함수
    enabled: !!name, // productId가 있을 때만 쿼리 실행
    staleTime: 5 * 60 * 1000,
  })
}