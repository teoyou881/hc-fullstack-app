// src/layout/UserLayout.jsx

import React from 'react';
import {Outlet} from 'react-router-dom';
import UserNavbar from './UserNavbar.jsx'; // UserNavbar 컴포넌트 임포트

const UserLayout = () => {
  return (
      <div>
        <UserNavbar/>
        <main style={{minHeight: '80vh', padding: '20px'}}>
          <Outlet/>
        </main>
      </div>
  );
};

export default UserLayout;