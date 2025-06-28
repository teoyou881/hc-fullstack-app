import {
  createApiInstance, createJwtResponseInterceptor, createRequestInterceptor,
} from './apiInterceptors.js';

const apiAdmin = createApiInstance(import.meta.env.VITE_ADMIN_URL);

// 요청 인터셉터 적용
apiAdmin.interceptors.request.use(...createRequestInterceptor());

// 응답 인터셉터 적용 (어드민용 - /adminLogin으로 리다이렉트)
apiAdmin.interceptors.response.use(...createJwtResponseInterceptor(apiAdmin, '/adminLogin'));

export default apiAdmin;
