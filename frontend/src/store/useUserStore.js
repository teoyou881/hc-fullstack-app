import {create} from 'zustand';
import apiClient from '../util/apiClient.js';
import userService from '../services/user/userService.js';

const useUserStore = create((set, get) => ({
  user: null,
  isAuthenticated: false,
  loading: false,
  authChecked: false,

  login: (userData) => {
    set({ user: userData.user, isAuthenticated: true, loading: false, authChecked: true });
  },

  setUser: (userData) => set({ user: userData, isAuthenticated: true, authChecked: true }),
  clearUser: () => set({ user: null, isAuthenticated: false, authChecked: true }),

  /**
   * 로그인 상태 확인 (토큰 갱신 포함)
   */
  checkLoginStatus: async () => {
    const { loading, authChecked } = get();

    if (loading || authChecked) return;

    // 현재 로그인 관련 페이지에 있으면 체크하지 않음
    const currentPath = window.location.pathname;
    const isAuthPage = currentPath === '/login' ||
        currentPath === '/adminLogin' ||
        currentPath === '/register';

    if (isAuthPage) {
      set({ authChecked: true });
      return;
    }

    set({ loading: true });

    try {
      const result = await userService.getUserInfo();
      if (result.success) {
        set({
          user: result.user,
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
      console.error('Failed to check login status:', error);
      set({
        user: null,
        isAuthenticated: false,
        authChecked: true
      });
    } finally {
      set({ loading: false });
    }
  },

  logout: async (navigate = null) => {
    set({ loading: true });
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout API error:', error);
    } finally {
      set({
        user: null,
        isAuthenticated: false,
        loading: false,
        authChecked: true
      });

      const currentPath = window.location.pathname;
      const isAdminPath = currentPath.startsWith('/admin');

      if (navigate) {
        navigate(isAdminPath ? '/adminLogin' : '/');
      } else {
        window.location.href = isAdminPath ? '/adminLogin' : '/';
      }
    }
  },

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