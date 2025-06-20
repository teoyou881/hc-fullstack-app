// src/pages/ProductDetailPage.jsx
import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { useProductById } from '../../hooks/products/useProducts.jsx'; // 훅 이름 유지

function ProductDetailPage() {
  const { productId } = useParams();
  const { data: skus, isLoading, isError, error } = useProductById(productId);

  const [selectedSkuIndex, setSelectedSkuIndex] = useState(0);

  useEffect(() => {
    if (skus && skus.length > 0) {
      setSelectedSkuIndex(0); // 첫 번째 SKU를 기본으로 선택
    }
  }, [skus]);

  // 현재 선택된 SKU 객체
  const selectedSku = skus?.[selectedSkuIndex];

  // useMemo를 사용하여 skus 데이터가 변경될 때만 계산
  const availableOptions = useMemo(() => {
    const options = {};
    skus?.forEach(sku => {
      const optionPart = sku.skuCode.split('-')[0];
      const optionGroupName = 'Color';
      const optionValue = optionPart;

      if (!options[optionGroupName]) {
        options[optionGroupName] = new Set();
      }
      options[optionGroupName].add(optionValue);
    });

    const result = {};
    for (const groupName in options) {
      result[groupName] = Array.from(options[groupName]);
    }
    return result;
  }, [skus]);


  const handleOptionSelect = (optionGroupName, selectedValue) => {
    const newSkuIndex = skus.findIndex(sku => {
      return sku.skuCode.split('-')[0] === selectedValue;
    });

    if (newSkuIndex !== -1) {
      setSelectedSkuIndex(newSkuIndex);
      // SKU가 변경될 때 이미지 갤러리가 맨 위로 스크롤되도록 할 수 있음
      // (페이지 전체 스크롤을 사용하므로, 스크롤 위치 조정은 필요 없을 수도 있습니다)
    } else {
      console.warn(`SKU not found for selected option: ${selectedValue}`);
    }
  };


  if (isLoading) {
    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
          <div className="text-xl font-semibold text-gray-700">제품 SKU 정보를 로딩 중입니다...</div>
        </div>
    );
  }

  if (isError) {
    return (
        <div className="flex justify-center items-center h-screen bg-red-100 text-red-700">
          <div className="text-xl font-semibold">
            제품 SKU 정보를 가져오는 데 실패했습니다: {error.message}
          </div>
        </div>
    );
  }

  if (!skus || skus.length === 0) {
    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
          <div className="text-xl font-semibold text-gray-700">해당 제품의 SKU를 찾을 수 없습니다.</div>
        </div>
    );
  }

  // 메인 상품의 공통 정보 (더미 데이터)
  const commonProductInfo = {
    title: `Amazing Product ${productId}`,
    brand: "Awesome Brand",
    category: "Cool Category",
    rating: 4.5
  };

  return (
      <div className="min-h-screen bg-white text-gray-800 p-8">
        {/* Header/Nav (Optional) */}

        <div className="max-w-6xl mx-auto mt-10 p-6 bg-white shadow-lg rounded-lg flex flex-col md:flex-row space-y-8 md:space-y-0 md:space-x-12">
          {/* Left Section: Single Image Gallery - NO INDIVIDUAL SCROLL */}
          {/* max-h와 overflow-y-auto 제거하여 개별 스크롤바 없앰 */}
          <div className="md:w-1/2 p-4 bg-gray-50 rounded-lg"> {/* Removed max-h and overflow-y-auto */}
            {selectedSku.images?.map((image, index) => (
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
                {commonProductInfo.title}
              </h1>
              <p className="text-gray-600 text-lg mb-2">SKU: <span className="font-semibold">{selectedSku.skuCode}</span></p>
              <p className="text-2xl font-bold text-gray-900 mb-6">
                ${selectedSku.price?.toFixed(2)}
              </p>
              <hr className="my-6 border-gray-300" />

              <div className="space-y-6 mb-8">
                {Object.entries(availableOptions).map(([optionGroupName, optionValues]) => (
                    <div key={optionGroupName}>
                      <h3 className="text-lg font-semibold text-gray-700 mb-3">{optionGroupName}: <span className="font-normal text-gray-900">{selectedSku.skuCode.split('-')[0]}</span></h3>
                      <div className="flex flex-wrap gap-3">
                        {optionValues.map(value => (
                            optionGroupName === 'Color' ? (
                                <div
                                    key={value}
                                    className={`w-10 h-10 rounded-full border-2 cursor-pointer flex items-center justify-center
                                        ${selectedSku.skuCode.split('-')[0] === value ? 'border-blue-500 ring-2 ring-blue-500' : 'border-gray-300 hover:border-gray-500'}`}
                                    style={{ backgroundColor: value.toLowerCase().replace(' ', '-') }}
                                    title={value}
                                    onClick={() => handleOptionSelect(optionGroupName, value)}
                                >
                                </div>
                            ) : (
                                <button
                                    key={value}
                                    className={`px-4 py-2 border rounded-full text-sm font-medium
                                        ${selectedSku.skuCode.split('-')[0] === value ? 'bg-blue-500 text-white border-blue-500' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'}`}
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

            <div className="mt-8">
              <button className="w-full bg-black hover:bg-gray-800 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-200 ease-in-out shadow-md">
                장바구니 담기
              </button>
            </div>
            <p className="text-gray-700 leading-relaxed mb-6 mt-6">{selectedSku.description}</p>
          </div>
        </div>
      </div>
  );
}

export default ProductDetailPage;