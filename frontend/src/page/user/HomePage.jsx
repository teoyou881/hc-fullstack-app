// src/pages/HomePage.jsx
import React from 'react';
import ProductCard from '../../component/ProductCard.jsx';
// Bootstrap 컴포넌트 (Col, Container, Row)는 더 이상 필요 없으므로 제거
// import { Col, Container, Row } from 'react-bootstrap';
import {useAllProducts} from '../../hooks/products/useProducts.jsx'; // useProducts.jsx -> useProducts.jsx로 변경

const HomePage = () => {
  const { data: productList, isLoading, isError, error } = useAllProducts();
  // 로딩 상태 처리
  if (isLoading) {
    return (
        <div className="flex justify-center items-center h-screen text-gray-700"> {/* Flexbox for centering, full screen height, gray text */}
          <p>Loading products...</p>
        </div>
    );
  }

  // 에러 상태 처리
  if (isError) {
    return (
        <div className="flex justify-center items-center h-screen text-red-600"> {/* Flexbox for centering, full screen height, red text */}
          <p>Error: {error?.message || "An unknown error occurred"}</p>
        </div>
    );
  }

  // 데이터가 없지만 에러는 아닌 경우 (예: 검색 결과 없음)
  if (!productList || productList.length === 0) {
    return (
        <div className="flex justify-center items-center h-screen text-gray-700"> {/* Flexbox for centering, full screen height, gray text */}
          <p>No products found.</p>
        </div>
    );
  }

  return (
      // <Container> 대신 div와 max-w, mx-auto, px 사용
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8"> {/* Max width, center, responsive padding, vertical padding */}
        {/* <Row> 대신 div와 flex, flex-wrap, -mx-N 사용 (Row의 gutter 효과) */}
        <div className="flex flex-wrap -mx-2 lg:-mx-4 "> {/* flex wrap for columns, negative margin for gutter effect */}
          {productList.map((item) => (
              // <Col> 대신 div와 w-1/N, px-N 사용 (Col의 width 및 padding 효과)
              <div
                  key={item.id} // key는 item.id를 사용
                  className="w-full sm:w-1/2 md:w-1/3 lg:w-1/4 px-2 mb-4 lg:px-4 cursor-pointer bg-lightgray" // Responsive widths, horizontal padding, margin bottom
              >
                <ProductCard product={item} />
              </div>
          ))}
        </div>
      </div>
  );
};

export default HomePage;