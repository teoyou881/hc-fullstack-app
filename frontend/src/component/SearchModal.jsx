import React, { useEffect, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimes } from '@fortawesome/free-solid-svg-icons';
import { motion } from 'framer-motion';
// 페이지 이동을 위해 react-router-dom의 useNavigate 훅을 import 합니다.
// 'react-router-dom'이 설치되어 있지 않다면, 먼저 npm install react-router-dom 또는 yarn add react-router-dom 명령으로 설치해주세요.
import { useNavigate } from 'react-router-dom'; // React Router v6용

// props로 isSearchOpen과 onClose 함수를 받습니다.
const SearchModal = ({ isSearchOpen, onClose }) => {
  const [productList, setProductList] = useState([]); // 검색 결과를 저장할 상태
  const [query, setQuery] = useState(''); // 사용자가 입력하는 검색어를 저장할 상태
  const navigate = useNavigate(); // 페이지 이동을 위한 useNavigate 훅 초기화

  // 현재 'query' 상태를 기반으로 상품을 가져오는 비동기 함수
  const getProducts = async (currentQuery) => {
    // 실제 검색어가 비어있으면 목록을 비우고 함수 종료 (불필요한 API 호출 방지)
    if (!currentQuery) {
      setProductList([]);
      return;
    }
    try {
      // 검색어를 포함하여 API 호출
      console.log("SearchModal.jsx: 24", currentQuery);
      const encodedQuery = encodeURIComponent(currentQuery);
      let url = `http://localhost:5000/products?q=${encodedQuery}`;
      console.log('Attempting to fetch with URL:', url); // <-- Add this line
      let response = await fetch(url);
      if (!response.ok) { // 응답이 성공적이지 않으면 에러 발생
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      let data = await response.json();
      console.log('Fetched products for live search:', data);
      setProductList(data); // 가져온 데이터를 productList 상태에 저장
    } catch (error) {
      console.error("상품을 가져오는 데 실패했습니다:", error);
      setProductList([]); // 에러 발생 시 목록 비우기
    }
  };

  // 입력 필드의 값이 변경될 때마다 query 상태를 업데이트
  const onChangeQuery = (e) => {
    setQuery(e.target.value);
  };

  // 입력 필드에서 Enter 키를 눌렀을 때 처리하는 함수
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') { // 눌린 키가 Enter인지 확인
      if (query.trim()) { // 검색어가 비어있지 않거나 공백만 있는 경우가 아니면
        onClose(); // 모달 닫기
        // 검색어를 인코딩하여 '/search' 경로로 이동합니다.
        // encodeURIComponent는 URL에 특수 문자가 포함될 경우를 대비합니다.
        navigate(`/hc_h_m/search?q=${encodeURIComponent(query.trim())}`);
      }
    }
  };

  // query 상태가 변경될 때마다 (사용자 타이핑 중) 실시간 검색 제안을 위한 useEffect
  useEffect(() => {
    // 모달이 열려 있고, 검색어가 비어있지 않은 경우에만 API 호출
    if (isSearchOpen && query.trim()) {
      // 디바운싱: 사용자가 타이핑을 멈춘 후 300ms 뒤에 API 호출
      // 이렇게 하면 모든 키 입력마다 API 호출을 방지하여 서버 부하를 줄일 수 있습니다.
      const debounceTimer = setTimeout(() => {
        getProducts(query.trim()) // 현재 query 상태를 getProducts 함수에 전달
      }, 300);

      // 클린업 함수: query가 변경되거나 컴포넌트가 언마운트될 때 이전 타이머를 제거
      return () => clearTimeout(debounceTimer);
    } else if (isSearchOpen && !query.trim()) {
      // 모달이 열려 있지만 검색어가 비어있으면 검색 결과 목록을 비웁니다.
      setProductList([]);
    }
  }, [query, isSearchOpen]); // query 또는 isSearchOpen 값이 변경될 때마다 이 useEffect 실행

  // isSearchOpen이 false이면 모달을 렌더링하지 않습니다.
  if (!isSearchOpen) {
    return null;
  }

  return (
      <>
        {/* 오버레이: 화면을 어둡게 하고 클릭 방지 */}
        <motion.div
            className="overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.5 }}
            exit={{ opacity: 0 }}
            onClick={onClose} // 오버레이 클릭 시 검색창 닫기
        />

        {/* 검색창: 오른쪽에서 나타나는 div */}
        <motion.div
            className="search-box"
            initial={{ x: '100%' }} // 화면 오른쪽 바깥에서 시작
            animate={{ x: 0 }} // 화면 안으로 이동
            exit={{ x: '100%' }} // 화면 오른쪽 바깥으로 사라짐
            transition={{ type: 'tween', duration: 0.3 }} // 부드러운 애니메이션
            style={{
              position: 'fixed',
              top: 0,
              right: 0,
              width: '30%', // 화면 너비의 30% (반응형 고려 필요)
              height: '100vh',
              backgroundColor: 'white',
              zIndex: 1000,
              padding: '20px',
              boxShadow: '-5px 0 15px rgba(0,0,0,0.2)', // 그림자 효과
              display: 'flex',
              flexDirection: 'column',
              gap: '20px',
            }}
        >
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}>
            <input
                type="text"
                placeholder="Search"
                style={{
                  border: 'none',
                  borderBottom: '1px solid gray',
                  width: '100%',
                  padding: '10px 0',
                  fontSize: '1.2em',
                }}
                value={query} // input의 값을 query 상태와 동기화 (Controlled Component)
                onChange={onChangeQuery} // 입력 값 변경 시 query 상태 업데이트
                onKeyPress={handleKeyPress} // Enter 키 감지
            />
            <button onClick={onClose} style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
            }}>
              <FontAwesomeIcon icon={faTimes} size="lg" /> {/* 닫기 버튼 */}
            </button>
          </div>

          {/* ⭐ 검색 결과 표시 부분 수정 */}
          <div className="search-results" style={{ overflowY: 'auto', flexGrow: 1 }}> {/* 스크롤 가능하도록 flexGrow 추가 */}
            {query.trim() && productList.length > 0 ? ( // 검색어가 있고, 결과가 있을 때
                <ul style={{ listStyle: 'none', padding: 0 }}>
                  {productList.map((item) => ( // 가져온 모든 상품 목록을 보여줌 (라이브 제안)
                      <li key={item.id} style={{
                        marginBottom: '10px',
                        padding: '8px',
                        borderBottom: '1px solid #eee',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                      }}
                          // 검색 제안 항목 클릭 시 해당 상품 상세 페이지로 이동 (선택적)
                          onClick={() => {
                            onClose(); // 모달 닫기
                            navigate(`/products/${item.id}`); // 예: /products/123 경로로 이동
                          }}
                      >
                        <img src={item.img} alt={item.title} style={{ width: '50px', height: '50px', objectFit: 'cover', marginRight: '10px', borderRadius: '4px' }} />
                        <div>
                          <div style={{ fontWeight: 'bold' }}>{item.title}</div>
                          <div style={{ color: '#888', fontSize: '0.9em' }}>₩{item.price}</div>
                        </div>
                      </li>
                  ))}
                </ul>
            ) : query.trim() && productList.length === 0 ? ( // 검색어가 있지만, 결과가 없을 때
                <p style={{ textAlign: 'center', color: '#888' }}>검색 결과가 없습니다.</p>
            ) : ( // 초기 상태 (검색어 없음)
                <p style={{ textAlign: 'center', color: '#888' }}>상품을 검색해보세요.</p>
            )}
          </div>

        </motion.div>
      </>
  );
};

export default SearchModal;