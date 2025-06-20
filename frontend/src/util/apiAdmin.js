import axios from 'axios';

const apiAdmin = axios.create({
  baseURL: import.meta.env.VITE_ADMIN_URL,
  withCredentials: true, // 쿠키 자동 전송
});

// 요청 인터셉터: Content-Type 자동 설정
apiAdmin.interceptors.request.use(
    (config) => {
      // FormData인 경우 Content-Type을 설정하지 않음 (브라우저가 자동 설정)
      if (config.data instanceof FormData) {
        // multipart/form-data는 브라우저가 boundary를 자동으로 설정해야 함
        delete config.headers['Content-Type'];
      } else {
        // 일반 데이터는 JSON으로 설정
        config.headers['Content-Type'] = 'application/json';
      }

      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

// 응답 인터셉터: 에러 처리
apiAdmin.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // 현재 경로가 로그인 페이지가 아닐 때만 리다이렉트
        const currentPath = window.location.pathname;
        if (currentPath !== `/login` && !currentPath.includes('/login')) {
          console.log('401 Error: Redirecting to login page');
          window.location.href = `/login`;
        }
      }
      return Promise.reject(error);
    }
);

export default apiAdmin;