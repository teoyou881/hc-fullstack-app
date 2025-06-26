import React, {useEffect, useState} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import apiClient from '../../util/apiClient.js';
import useUserStore from '../../store/useUserStore.js';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const nav = useNavigate();
  const location = useLocation();

  // Zustand Store의 login 액션 가져오기
  const loginUser = useUserStore((state) => state.login);
  const isAuthenticated = useUserStore((state) => state.user !== null);

  // 로그인 성공 후 이동할 경로 결정
  const getRedirectPath = () => {
    // state에서 from 경로가 있다면 그곳으로, 없다면 현재 URL 확인
    const from = location.state?.from?.pathname;
    
    if (from) {
      return from;
    }
    
    // 현재 URL이 /adminLogin이면 admin으로 간주
    if (location.pathname === '/adminLogin') {
      return '/admin/';
    }
    
    // 기본값은 홈페이지
    return '/';
  };

  // handle submit
  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await apiClient.post('/login', {
        email: email,
        password: password,
      });
      const { user } = response.data;

      // Zustand Store에 사용자 정보 저장
      loginUser(user);

      // 적절한 경로로 리다이렉트
      const redirectPath = getRedirectPath();
      nav(redirectPath, { replace: true });

    } catch (error) {
      console.error('Login failed:', error.response ? error.response.data : error.message);
      alert('로그인 실패: ' + (error.response?.data?.message || '서버 오류'));
    }
  };

  useEffect(() => {
    // 이미 로그인되어 있다면 적절한 경로로 리다이렉트
    if (isAuthenticated) {
      const redirectPath = getRedirectPath();
      nav(redirectPath, { replace: true });
    }
  }, [isAuthenticated, nav]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="bg-white p-10 rounded-lg shadow-xl w-full max-w-md box-border">
        <h2 className="text-center text-3xl font-bold mb-8 text-gray-800">
          {location.pathname === '/adminLogin' ? 'Admin Login' : 'Login'}
        </h2>
        <form className="flex flex-col" onSubmit={handleSubmit}>
          <div className="mb-4">
            <p className="mb-1 text-gray-700 text-sm">Email address</p>
            <input
              type="text"
              placeholder="Enter email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-base"
            />
          </div>
          <div className="mb-6">
            <p className="mb-1 text-gray-700 text-sm">Password</p>
            <input
              type="password"
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-base"
            />
          </div>
          <button
            type="submit"
            className="w-full py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 transition-colors duration-200 text-lg cursor-pointer"
          >
            Login
          </button>
        </form>

        <div className="flex justify-between items-center mt-6 text-sm">
          <div className="flex space-x-4">
            <a href="#" className="text-blue-600 hover:underline">Forgot ID?</a>
            <a href="#" className="text-blue-600 hover:underline">Forgot Password?</a>
          </div>
          <button
            onClick={() => nav('/register')}
            className="text-blue-600 hover:underline px-0 py-0 text-sm"
          >
            Register
          </button>
        </div>
      </div>
    </div>
  );
};

export default Login;