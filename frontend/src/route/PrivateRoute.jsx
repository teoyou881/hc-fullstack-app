import React, {useEffect, useRef} from 'react';
import {Navigate, Outlet} from 'react-router-dom';

function PrivateRoute({ isAuthenticated, authChecked, userRole, requiredRoles, forceLogout }) {
  const unauthorizedHandled = useRef(false);

  useEffect(() => {
    // 권한이 없고, 아직 처리되지 않았다면
    if (
        authChecked &&
        isAuthenticated &&
        requiredRoles &&
        !requiredRoles.includes(userRole) &&
        !unauthorizedHandled.current
    ) {
      unauthorizedHandled.current = true;
      alert("접근 권한이 없습니다.");
      if (forceLogout) {
        forceLogout('/adminLogin');
      }
    }
  }, [authChecked, isAuthenticated, userRole, requiredRoles, forceLogout]);

  if (!authChecked) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/adminLogin" replace />;
  }

  if (requiredRoles && !requiredRoles.includes(userRole)) {
    return null;
  }

  return <Outlet />;
}

export default PrivateRoute;
