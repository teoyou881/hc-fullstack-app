import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_USER_URL,
  withCredentials: true,
});

// 요청 인터셉터
apiClient.interceptors.request.use(
    (config) => {
      if (config.data instanceof FormData) {
        delete config.headers['Content-Type'];
      } else {
        config.headers['Content-Type'] = 'application/json';
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        const currentPath = window.location.pathname;

        // 이미 인증 관련 페이지이거나 인증 체크 API인 경우 리다이렉트하지 않음
        const isAuthPage = currentPath.includes('/login')
            || currentPath.includes('/register');
        const isAuthCheckAPI = error.config?.url === '/user'; // 인증 상태 확인 API

        if (!isAuthPage && !isAuthCheckAPI) {
          console.log('401 Error: Redirecting to login page');
          window.location.href = `/login`;
        }
      }
      return Promise.reject(error);
    }
);

export default apiClient;