// src/layout/AdminLayout.jsx (새로 생성)
import React from 'react';
import {Link, Outlet, useLocation} from 'react-router-dom';
import {Col, Container, Nav, Row} from 'react-bootstrap';
import './AdminLayout.css';
import AdminNavbar from './AdminNavbar.jsx'; // 사이드바 스타일링을 위한 CSS 파일

function AdminLayout() {
  const location = useLocation();

  return (
      <Container fluid className="admin-layout">
        <AdminNavbar></AdminNavbar>
        <Row>
          {/* 사이드바 */}
          <Col xs={2} id="sidebar-wrapper" className="bg-light border-end">
            <Nav className="flex-column p-3">
              {/* 상품 관리 하위 메뉴 */}
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/products" active={location.pathname.startsWith('/admin/products')}>
                  상품 목록
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/products/new" active={location.pathname === '/admin/products/new'}>
                  상품 등록
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/categories" active={location.pathname.startsWith('/admin/categories')}>
                  카테고리 관리
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/options/types" active={location.pathname.startsWith('/admin/options/types')}>
                  옵션 관리
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/options/values" active={location.pathname.startsWith('/admin/options/values')}>
                  값 관리
                </Nav.Link>
              </Nav.Item>

              {/* 회원 관리 하위 메뉴 (예시) */}
              <hr />
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/users" active={location.pathname===('/admin/users')}>
                  전체 회원
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link as={Link} to="/admin/users/roles" active={location.pathname.startsWith('/admin/users/roles')}>
                  권한 설정
                </Nav.Link>
              </Nav.Item>
              {/* ... 기타 하위 메뉴 */}
            </Nav>
          </Col>

          {/* 메인 콘텐츠 영역 */}
          <Col xs={10} id="page-content-wrapper">
            <Outlet /> {/* 중첩된 라우트의 컴포넌트가 여기에 렌더링됩니다. */}
          </Col>
        </Row>
      </Container>
  );
}

export default AdminLayout;