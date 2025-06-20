import React from 'react';
import {useNavigate} from 'react-router-dom';

const ProductCard = ({product}) => {

  const nav = useNavigate();

  const showDetail = () => {
    nav(`/product/${product.id}`);
  };
  return (
      <div className="product-card">
        <img src={product.thumbnailUrl} alt={product.name} className="product-image"
             onClick={showDetail}/>
        {/* alt 속성을 추가하는 것이 좋습니다. */}
        <div>{product?.new == true ? 'Designer Recommended' : ''}</div>
        <div>{product.name}</div>
        {/* JavaScript 변수는 중괄호로 감싸야 합니다. */}
        <div>${product.minPrice}</div>
        <div>{product?.new == true ? 'New' : ''}</div>
      </div>
  );
};

export default ProductCard;