// src/store/useUserStore.js
import {create} from 'zustand';
import apiClient from '../util/apiClient.js'; // API 클라이언트 임포트

const useUserStore = create((set, get) => ({
  //- **`isAuthenticated`**: "사용자가 로그인되어 있나요?"
  // - **`authChecked`**: "서버에 확인해봤나요?"
  user: null,
  isAuthenticated: false,
  loading: false,
  authChecked: false, // 인증 체크 완료 여부

  // ⭐ 새로운 login 액션 추가 ⭐
  login: (userData) => {
    set({ user: userData, isAuthenticated: true, loading: false, authChecked: true });
  },

  setUser: (userData) => set({ user: userData, isAuthenticated: true, authChecked: true }),
  clearUser: () => set({ user: null, isAuthenticated: false, authChecked: true }),

  checkLoginStatus: async () => {
    const { loading, authChecked } = get();
    
    // 이미 인증 체크가 완료되었거나 로딩 중이면 중복 요청 방지
    if (loading || authChecked) return;
    
    set({ loading: true });
    try {
      const response = await apiClient.get('/user');

      if (response.data.success && (response.data.user)) {
        set({ 
          user: response.data.user, 
          isAuthenticated: true, 
          authChecked: true 
        });
      } else {
        set({ 
          user: null, 
          isAuthenticated: false, 
          authChecked: true 
        });
      }
    } catch (error) {
      console.error('Failed to fetch user info or token invalid:', error);
      
      // 네트워크 에러인 경우 인증되지 않은 상태로 처리
      if (error.code === 'ERR_NETWORK' || error.code === 'ERR_CONNECTION_REFUSED') {
        console.warn('Backend server not available, setting as unauthenticated');
      }
      
      set({ 
        user: null, 
        isAuthenticated: false, 
        authChecked: true // 에러가 발생해도 체크 완료로 처리
      });
    } finally {
      set({ loading: false });
    }
  },

  logout: async (navigate = null) => {
    set({ loading: true });
    try {
      await apiClient.post('/logout');
    } catch (error) {
      console.error('Logout API error:', error);
    } finally {
      set({ 
        user: null, 
        isAuthenticated: false, 
        loading: false, 
        authChecked: true 
      });

      if (navigate) {
        navigate('/');
      } else {
        window.location.href = '/';
      }
    }
  },

  // 인증 상태 초기화 (개발용)
  resetAuthState: () => {
    set({ 
      user: null, 
      isAuthenticated: false, 
      loading: false, 
      authChecked: false 
    });
  }
}));

export default useUserStore;