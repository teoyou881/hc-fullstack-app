// ... (생략된 import 및 공통 코드 포함)
import axios from 'axios';

// 토큰 갱신 관련 공통 상태
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 로그인 관련 페이지인지 확인하는 함수
const isAuthPage = () => {
  const currentPath = window.location.pathname;
  return currentPath === '/login' ||
      currentPath === '/adminLogin' ||
      currentPath === '/register';
};

// 요청 URL 또는 현재 페이지가 admin 관련인지 확인하는 함수
const isAdminRequest = (requestUrl = '') => {
  const currentPath = window.location.pathname;
  const adminUrl = import.meta.env.VITE_ADMIN_URL || '/api/admin';

  if (requestUrl.includes('/admin') || requestUrl.includes(adminUrl)) {
    return true;
  }

  if (currentPath.startsWith('/admin')) {
    return true;
  }

  if (currentPath === '/adminLogin') {
    return true;
  }

  return false;
};

// 적절한 로그인 페이지로 리다이렉트하는 함수
const redirectToLogin = (requestUrl = '') => {
  if (isAuthPage()) {
    return;
  }
  const targetLoginPage = isAdminRequest(requestUrl) ? '/adminLogin' : '/login';
  console.log('Redirecting to:', targetLoginPage);
  window.location.href = targetLoginPage;
};

// 요청 인터셉터 팩토리
export const createRequestInterceptor = () => {
  return [
    (config) => {
      if (config.data instanceof FormData) {
        delete config.headers['Content-Type'];
      } else {
        config.headers['Content-Type'] = 'application/json';
      }
      return config;
    },
    (error) => Promise.reject(error)
  ];
};

// 응답 인터셉터 팩토리
export const createJwtResponseInterceptor = (apiInstance) => {
  return [
    (response) => response,
    async (error) => {
      const originalRequest = error.config;
      const requestUrl = originalRequest?.url || '';

      if (error.response?.status === 401 && !originalRequest._retry) {
        const errorData = error.response.data;

        if (isAuthPage()) {
          return Promise.reject(error);
        }

        if (errorData?.code === 'TOKEN_REFRESH_REQUIRED') {
          if (isRefreshing) {
            return new Promise((resolve, reject) => {
              failedQueue.push({ resolve, reject });
            }).then(() => apiInstance(originalRequest))
            .catch(err => Promise.reject(err));
          }

          originalRequest._retry = true;
          isRefreshing = true;

          try {
            await apiInstance.post('/auth/refresh');
            processQueue(null);
            return apiInstance(originalRequest);
          } catch (refreshError) {
            processQueue(refreshError, null);
            redirectToLogin(requestUrl);
            return Promise.reject(refreshError);
          } finally {
            isRefreshing = false;
          }
        }

        if (errorData?.code === 'AUTHENTICATION_REQUIRED' || !errorData?.code) {
          redirectToLogin(requestUrl);
        }
      }

      return Promise.reject(error);
    }
  ];
};

// API 인스턴스 생성 헬퍼 함수
export const createApiInstance = (baseURL, options = {}) => {
  const instance = axios.create({
    baseURL,
    withCredentials: true,
    ...options
  });

  return instance;
};
