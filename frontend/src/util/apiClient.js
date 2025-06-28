import {
  createApiInstance, createJwtResponseInterceptor, createRequestInterceptor,
} from './apiInterceptors.js';

const apiClient = createApiInstance(import.meta.env.VITE_USER_URL);

// 요청 인터셉터 적용
apiClient.interceptors.request.use(...createRequestInterceptor());

// 응답 인터셉터 적용 (클라이언트용 - /login으로 리다이렉트)
apiClient.interceptors.response.use(...createJwtResponseInterceptor(apiClient, '/login'));

export default apiClient;
