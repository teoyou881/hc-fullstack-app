import React from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {Button, Container, Nav, Navbar} from 'react-bootstrap';
import {
  FaBoxes, FaChartBar, FaCog, FaShoppingCart, FaUsers,
} from 'react-icons/fa';
import useUserStore from '../store/useUserStore.js'; // 아이콘 추가

function AdminNavbar() {
  const navigate = useNavigate();
  const {logout } = useUserStore();

  const handleLogoutClick = async() => {
    await logout(navigate);
  };

  return (
      <Navbar bg="dark" variant="dark" expand="lg" className="admin-navbar">
        <Container fluid>
          <Navbar.Brand as={Link} to="/admin">Admin Dashboard</Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="ms-auto d-flex align-items-center me-3">
              {/* 주요 기능 단위 메뉴 */}
              <Nav.Link as={Link} to="/admin/products">
                <FaBoxes className="me-1" /> 상품 관리
              </Nav.Link>
              <Nav.Link as={Link} to="/admin/users">
                <FaUsers className="me-1" /> 회원 관리
              </Nav.Link>
              <Nav.Link as={Link} to="/admin/orders">
                <FaShoppingCart className="me-1" /> 주문 관리
              </Nav.Link>
              <Nav.Link as={Link} to="/admin/statistics">
                <FaChartBar className="me-1" /> 통계
              </Nav.Link>
              <Nav.Link as={Link} to="/admin/settings">
                <FaCog className="me-1" /> 설정
              </Nav.Link>
            </Nav>
            <Nav>
              <Button variant="outline-light" onClick={handleLogoutClick}>
                로그아웃 (Admin)
              </Button>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
  );
}

export default AdminNavbar;