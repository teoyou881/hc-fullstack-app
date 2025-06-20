// src/route/PrivateRoute.jsx
import React from 'react';
import {Navigate, Outlet} from 'react-router-dom';

function PrivateRoute({ isAuthenticated, userRole, requiredRoles }) {
  // 사용자가 로그인되어 있지 않다면 로그인 페이지로 리다이렉트
  if (!isAuthenticated) {
    // Admin 페이지로 접근하려 했을 때, 로그인 성공 후 다시 Admin 페이지로 돌아가도록 state를 전달할 수 있습니다.
    // 하지만 여기서는 단순히 /login으로 리다이렉트합니다.
    return <Navigate to="/adminLogin" replace />;
  }

  // 사용자가 로그인되어 있다면, 역할(role)을 확인합니다.
  // requiredRoles가 정의되어 있고, 사용자의 역할이 필요한 역할 목록에 포함되어 있지 않다면 접근을 거부합니다.
  // userRole이 null이거나 undefined인 경우를 대비하여 방어 코드를 추가합니다.
  if (requiredRoles && !requiredRoles.includes(userRole)) {
    // 권한이 없는 경우 홈 페이지 또는 권한 없음 페이지로 리다이렉트
    // 실제 애플리케이션에서는 403 Forbidden 페이지 등을 보여줄 수 있습니다.
    alert("접근 권한이 없습니다."); // 임시 알림
    return <Navigate to="/adminLogin" replace />;
  }

  // 모든 조건을 통과했다면, 자식 라우트(AdminLayout 및 Admin 페이지들)를 렌더링합니다.
  return <Outlet />;
}

export default PrivateRoute;