import React, {useState} from 'react';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import {
  faSearch, faSignOutAlt, faUser,
} from '@fortawesome/free-solid-svg-icons';
import logo from '../assets/logo_black.png';
import {AnimatePresence} from 'framer-motion';

import SearchModal from '../component/SearchModal.jsx';
import {useNavigate} from 'react-router-dom';
import useUserStore from '../store/useUserStore.js';

const UserNavbar = () => {
  const menuList = [
    "WOMEN", "MEN", "JEWELRY", "GIFT", "COLLECTION"
  ];

  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const navigate = useNavigate();

  const { user, isAuthenticated, logout } = useUserStore();

  const toggleSearch = () => {
    setIsSearchOpen(!isSearchOpen);
  };

  const handleLoginClick = () => {
    navigate('/login');
  };

  const handleLogoutClick = () => {
    logout(navigate);
  };

  return (
    <div className="bg-white shadow-md">
      {/* Top button area */}
      <div className="flex justify-end items-center px-4 py-2 border-b border-gray-200">
        {isAuthenticated ? (
          <>
            <div className="flex items-center mr-4 text-sm text-gray-700">
              <FontAwesomeIcon icon={faUser} className="mr-1" />
              {user && user.email}님
            </div>
            <button 
              className="flex items-center text-sm text-gray-700 hover:text-black transition-colors mr-4"
              onClick={handleLogoutClick}
            >
              <FontAwesomeIcon icon={faSignOutAlt} className="mr-1" /> 
              로그아웃
            </button>
          </>
        ) : (
          <button 
            className="flex items-center text-sm text-gray-700 hover:text-black transition-colors mr-4"
            onClick={handleLoginClick}
          >
            <FontAwesomeIcon icon={faUser} className="mr-1" /> 
            로그인
          </button>
        )}
        
        <button 
          onClick={toggleSearch}
          className="text-gray-700 hover:text-black transition-colors"
        >
          <FontAwesomeIcon icon={faSearch} />
        </button>
      </div>

      {/* Logo section */}
      <div className="flex justify-center py-4">
        <img 
          src={logo} 
          alt="logo"
          className="h-28 w-auto cursor-pointer hover:opacity-90 transition-opacity"
          onClick={() => navigate('/')}
        />
      </div>

      {/* Menu area */}
      <div className="bg-gray-50 border-t border-gray-200">
        <div className="max-w-7xl mx-auto px-4">
          <ul className="flex justify-center space-x-8 py-3">
            {menuList.map((item, index) => (
              <li 
                key={index}
                className="text-sm font-medium text-gray-700 hover:text-black cursor-pointer transition-colors"
              >
                {item}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <AnimatePresence>
        <SearchModal isSearchOpen={isSearchOpen} onClose={toggleSearch} />
      </AnimatePresence>
    </div>
  );
};

export default UserNavbar;