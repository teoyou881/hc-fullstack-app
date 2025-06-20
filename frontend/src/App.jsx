import React, {useEffect} from 'react';
import {Route, Routes} from 'react-router-dom';
import useUserStore from './store/useUserStore.js';
import 'bootstrap/dist/css/bootstrap.min.css';
// Import your components
import HomePage from './page/user/HomePage.jsx';
import UserLayout from './layout/UserLayout.jsx';
import Login from './page/auth/Login.jsx';
import RegisterForm from './page/auth/RegisterForm.jsx';
import Search from './page/user/Search.jsx';
import ProductDetail from './page/user/ProductDetail.jsx';
import PrivateRoute from './route/PrivateRoute.jsx';
import AdminLayout from './layout/AdminLayout.jsx';
import AdminHome from './page/admin/AdminHome.jsx';
import AdminProductListPage
  from './page/admin/products/AdminProductListPage.jsx';
import AdminSkuListPage from './page/admin/products/AdminSkuListPage.jsx';
import AdminProductCreatePage
  from './page/admin/products/AdminProductCreatePage.jsx';
import AdminSkuPage from './page/admin/AdminSkuPage.jsx';
import AdminProductEditPage
  from './page/admin/products/AdminProductEditPage.jsx';
import AdminCategoryPage from './page/admin/categories/AdminCategoryPage.jsx';
import AdminOptionTypeList
  from './page/admin/options/AdminOptionTypeList.jsx.jsx';
import AdminOptionValueList
  from './page/admin/options/AdminOptionValueList.jsx';
import AdminUserListPage from './page/admin/users/AdminUserListPage.jsx';

const App = () => {
  const {user, isAuthenticated, loading, authChecked, checkLoginStatus} = useUserStore();

  useEffect(() => {
    // 컴포넌트 마운트 시 인증 상태 확인
    if (!authChecked) {
      checkLoginStatus();
    }
  }, [checkLoginStatus, authChecked]);

  // 인증 체크가 완료되지 않았고 로딩 중일 때만 로딩 화면 표시
  if (!authChecked && loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="sr-only">Loading...</span>
          </div>
          <p className="mt-2">Loading authentication status...</p>
        </div>
      </div>
    );
  }

  const userRole = user ? user.role : "ROLE_GUEST";

  return (
    <div className="App">
      <Routes>
        {/* 관리자 관련 라우트 - 우선순위를 위해 먼저 배치 */}
        <Route element={<PrivateRoute isAuthenticated={isAuthenticated} userRole={userRole} requiredRoles={["ROLE_ADMIN", "ROLE_MANAGER"]} />}>
          <Route path="/admin/*" element={<AdminLayout />}>
            <Route index element={<AdminHome />} />
            <Route path="products" element={<AdminProductListPage />} />
            <Route path="products/:productId/sku" element={<AdminSkuListPage />} />
            <Route path="products/new" element={<AdminProductCreatePage />} />
            <Route path="sku/:skuId" element={<AdminSkuPage />} />
            <Route path="products/edit/:id" element={<AdminProductEditPage />} />
            <Route path="categories" element={<AdminCategoryPage />} />
            <Route path="options/types" element={<AdminOptionTypeList />} />
            <Route path="options/values" element={<AdminOptionValueList />} />
            <Route path="users" element={<AdminUserListPage />} />
          </Route>
        </Route>
        <Route path="adminLogin" element={<Login />} />
        {/* 사용자 관련 라우트 - 구체적인 경로들만 포함 */}
        <Route path="/" element={<UserLayout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<Login />} />
          <Route path="register" element={<RegisterForm />} />
          <Route path="search" element={<Search />} />
          <Route path="product/:productId" element={<ProductDetail />} />
          <Route path="women" element={<div>Women 페이지</div>} />
          <Route path="men" element={<div>Men 페이지</div>} />
          <Route path="jewelry" element={<div>Jewelry 페이지</div>} />
          <Route path="gift" element={<div>Gift 페이지</div>} />
          <Route path="collection" element={<div>Collection 페이지</div>} />
        </Route>
      </Routes>
    </div>
  );
};

export default App;