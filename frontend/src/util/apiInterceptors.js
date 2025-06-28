// ... (생략된 import 및 공통 코드 포함)
import axios from 'axios';
import apiClient from './apiClient.js';

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

// 사용자 정보 초기화 및 로그아웃 처리
const clearUserDataAndLogout = async (requestUrl = '') => {
  if (isAuthPage()) {
    return; // 이미 로그인 페이지에 있으면 처리하지 않음
  }

  try {
    // 1. 백엔드에 로그아웃 요청 (JWT 토큰 무효화 및 쿠키 삭제)
    await apiClient.post('/auth/logout').catch(() => {
      // 로그아웃 API 실패해도 프론트엔드 정리는 계속 진행
      console.warn('Logout API failed, but continuing with frontend cleanup');
    });
  } catch (error) {
    console.error('Error during logout:', error);
  }

  // 2. Zustand store 초기화 (dynamic import로 순환 참조 방지)
  try {
    const { default: useUserStore } = await import('../store/useUserStore.js');
    useUserStore.getState().resetAuthState();
  } catch (error) {
    console.error('Error clearing user store:', error);
  }

  // 3. 적절한 로그인 페이지로 리다이렉트
  redirectToLogin(requestUrl);
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
    console.log("apiInterceptors.js: 112", error);
      const originalRequest = error.config;
      const requestUrl = originalRequest?.url || '';

      if (error.response?.status === 401 && !originalRequest._retry) {
        const errorData = error.response.data;

        // 현재 로그인 페이지에 있으면 리다이렉트하지 않음
        if (isAuthPage()) {
          return Promise.reject(error);
        }

        // 백엔드에서 TOKEN_REFRESH_REQUIRED 응답을 보낸 경우
        if (errorData?.code === 'TOKEN_REFRESH_REQUIRED') {
          if (isRefreshing) {
            return new Promise((resolve, reject) => {
              failedQueue.push({ resolve, reject });
            }).then(() => {
              return apiClient(originalRequest);
            }).catch(err => {
              return Promise.reject(err);
            });
          }

          originalRequest._retry = true;
          isRefreshing = true;

          try {
            await apiClient.post('/auth/refresh');
            processQueue(null);
            return apiClient(originalRequest);
          } catch (refreshError) {
            processQueue(refreshError, null);

            // 토큰 갱신 실패 시 사용자 데이터 초기화 및 로그아웃
            await clearUserDataAndLogout(requestUrl);

            return Promise.reject(refreshError);
          } finally {
            isRefreshing = false;
          }
        }

        // AUTHENTICATION_REQUIRED 또는 기타 401 에러의 경우
        if (errorData?.code === 'AUTHENTICATION_REQUIRED' || !errorData?.code) {
          await clearUserDataAndLogout(requestUrl);
        }
      }

      // 403 Forbidden (접근 권한 없음)의 경우도 처리
      if (error.response?.status === 403) {
        console.warn('Access forbidden - clearing user data');
        await clearUserDataAndLogout(requestUrl);
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
