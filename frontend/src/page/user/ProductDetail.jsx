import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {useProductById} from '../../hooks/products/useProducts.jsx'; // 훅 이름 유지

function ProductDetailPage() {
  const { productId } = useParams();
  const { data, isLoading, isError, error } = useProductById(productId);

  // 현재 선택된 SKU의 인덱스를 관리 (화면 표시용)
  const [selectedSkuIndex, setSelectedSkuIndex] = useState(0);
  // 현재 선택된 색상과 사이즈를 독립적으로 관리
  const [selectedColor, setSelectedColor] = useState(null);
  const [selectedSize, setSelectedSize] = useState(null);

  // data 객체에서 필요한 정보 추출
  const title = data?.title;
  const sizes = data?.sizes || [];
  const colors = data?.colors || [];
  const skus = data?.skus || []; // 원본 product details를 skus로 사용

  // 데이터 로드 시 초기 선택 값 설정
  useEffect(() => {
    if (skus && skus.length > 0) {
      // 첫 번째 SKU의 색상과 사이즈를 초기 선택 값으로 설정
      const initialSku = skus[0];
      const initialSize = initialSku.skuCode.split('-')[1];
      const initialColor = initialSku.skuCode.split('-')[2];

      setSelectedSize(initialSize);
      setSelectedColor(initialColor);
      setSelectedSkuIndex(0); // 첫 번째 SKU를 기본으로 선택
    }
  }, [skus]); // skus가 변경될 때마다 실행 (데이터 로드 시)

  // selectedColor 또는 selectedSize가 변경될 때마다 적절한 SKU를 찾습니다.
  useEffect(() => {
    if (selectedColor && selectedSize && skus.length > 0) {
      const newSkuIndex = skus.findIndex(sku => {
        const skuSize = sku.skuCode.split('-')[1];
        const skuColor = sku.skuCode.split('-')[2];
        return skuSize === selectedSize && skuColor === selectedColor;
      });

      if (newSkuIndex !== -1) {
        setSelectedSkuIndex(newSkuIndex);
      } else {
        // 일치하는 SKU를 찾지 못한 경우 (예: 특정 색상/사이즈 조합이 없는 경우)
        // 이 경우 사용자에게 메시지를 보여주거나, 다른 기본값을 설정하는 로직 필요
        console.warn(`No SKU found for selected combination: Size=${selectedSize}, Color=${selectedColor}`);
        // 예를 들어, 매치되는 SKU가 없으면 selectedSkuIndex를 -1로 설정하여
        // "조합 없음" 메시지를 표시하거나, 가장 가까운 SKU를 선택할 수 있습니다.
        // setSelectedSkuIndex(-1);
      }
    }
  }, [selectedColor, selectedSize, skus]);


  // 현재 선택된 SKU 객체 (selectedSkuIndex가 -1이 아닐 때만 유효)
  const selectedSku = skus?.[selectedSkuIndex];

  // 사용 가능한 옵션 그룹 정의 (렌더링에 사용)
  const availableOptions = {
    'Color': colors,
    'Size': sizes,
  };

  const handleOptionSelect = (optionGroupName, value) => {
    console.log(`선택된 옵션: ${optionGroupName} = ${value}`);

    if (optionGroupName === 'Color') {
      setSelectedColor(value); // 선택된 색상만 업데이트
    } else if (optionGroupName === 'Size') {
      setSelectedSize(value); // 선택된 사이즈만 업데이트
    }
    // useEffect에서 selectedColor/selectedSize 변경을 감지하여 selectedSkuIndex 업데이트
  };

  if (isLoading) {
    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
          <div className="text-xl font-semibold text-gray-700">제품 정보를 로딩 중입니다...</div>
        </div>
    );
  }

  if (isError) {
    return (
        <div className="flex justify-center items-center h-screen bg-red-100 text-red-700">
          <div className="text-xl font-semibold">
            제품 정보를 가져오는 데 실패했습니다: {error.message}
          </div>
        </div>
    );
  }

  if (!data || !skus || skus.length === 0) {
    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
          <div className="text-xl font-semibold text-gray-700">해당 제품을 찾을 수 없습니다.</div>
        </div>
    );
  }

  return (
      <div className="min-h-screen bg-white text-gray-800 p-8">
        <div className="max-w-6xl mx-auto mt-10 p-6 bg-white shadow-lg rounded-lg flex flex-col md:flex-row space-y-8 md:space-y-0 md:space-x-12">
          {/* Left Section: Single Image Gallery */}
          <div className="md:w-1/2 p-4 bg-gray-50 rounded-lg">
            {selectedSku?.images?.map((image, index) => (
                <div key={image.id || index} className="w-full mb-4 flex justify-center items-center rounded-lg shadow-md">
                  <img
                      src={image.imageUrl || 'https://via.placeholder.com/600/bbbbbb'}
                      alt={`${selectedSku.skuCode} - ${index + 1}`}
                      className="max-w-full h-auto object-contain"
                  />
                </div>
            ))}
          </div>

          {/* Right Section: Product Details for Selected SKU */}
          <div className="md:w-1/2 flex flex-col p-4">
            <div>
              <h1 className="text-4xl font-extrabold text-gray-900 mb-2">
                {title}
              </h1>
              {/* 선택된 SKU가 없을 경우 (조합 없음) 처리 */}
              {selectedSku ? (
                  <>
                    <p className="text-gray-600 text-lg mb-2">SKU: <span className="font-semibold">{selectedSku.skuCode}</span></p>
                    <p className="text-2xl font-bold text-gray-900 mb-6">
                      ${selectedSku.price?.toFixed(2)}
                    </p>
                  </>
              ) : (
                  <p className="text-xl font-semibold text-red-600 mb-6">선택된 옵션 조합의 재고가 없습니다.</p>
              )}

              <hr className="my-6 border-gray-300" />

              <div className="space-y-6 mb-8">
                {Object.entries(availableOptions).map(([optionGroupName, optionValues]) => (
                    <div key={optionGroupName}>
                      <h3 className="text-lg font-semibold text-gray-700 mb-3">
                        {optionGroupName}:
                        {/* 현재 선택된 옵션 값 표시 */}
                        <span className="font-normal text-gray-900 ml-2">
                          {optionGroupName === 'Color' ? selectedColor : selectedSize}
                        </span>
                      </h3>
                      <div className="flex flex-wrap gap-3">
                        {optionValues.map(value => (
                            optionGroupName === 'Color' ? (
                                <div
                                    key={value}
                                    className={`w-10 h-10 rounded-full border-2 cursor-pointer flex items-center justify-center
                                        ${selectedColor === value ? 'border-blue-500 ring-2 ring-blue-500' : 'border-gray-300 hover:border-gray-500'}`}
                                    style={{ backgroundColor: value.toLowerCase().replace(' ', '-') }}
                                    title={value}
                                    onClick={() => handleOptionSelect(optionGroupName, value)}
                                >
                                </div>
                            ) : (
                                <button
                                    key={value}
                                    className={`px-4 py-2 border rounded-full text-sm font-medium
                                        ${selectedSize === value ? 'bg-blue-500 text-white border-blue-500' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'}`}
                                    onClick={() => handleOptionSelect(optionGroupName, value)}
                                >
                                  {value}
                                </button>
                            )
                        ))}
                      </div>
                    </div>
                ))}
              </div>
            </div>

            {selectedSku ? ( // 선택된 SKU가 있을 때만 장바구니 버튼과 설명을 표시
                <>
                  <div className="mt-8">
                    <button className="w-full bg-black hover:bg-gray-800 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-200 ease-in-out shadow-md">
                      장바구니 담기
                    </button>
                  </div>
                  <p className="text-gray-700 leading-relaxed mb-6 mt-6">{selectedSku.description}</p>
                </>
            ) : null}
          </div>
        </div>
      </div>
  );
}

export default ProductDetailPage;