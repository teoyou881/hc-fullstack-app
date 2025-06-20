import axios from 'axios';

const VITE_ADMIN_TYPE_URL = import.meta.env.VITE_ADMIN_URL + '/options/type';

const optionTypeService = {
  getAllOptionTypes: async() => {
    return await axios.get(VITE_ADMIN_TYPE_URL);
  },
  getOptionTypeById: (id) => {
    return axios.get(`${VITE_ADMIN_TYPE_URL}/${id}`);
  },

  createOptionType:async  (optionTypeData, optionTypes) => {
    // 1. 클라이언트 측 유효성 검사: displayOrder 중복 확인 (프론트엔드에 로드된 데이터 기준)
    // 현재 프론트엔드에 존재하는 optionTypes 목록에서 displayOrder 중복을 확인
    if (optionTypes.filter(type => type.displayOrder === optionTypeData.displayOrder).length > 0) {
      // 에러 발생: 백엔드에 요청을 보내지 않고 프론트엔드에서 즉시 에러 반환
      throw new Error('Display order already exists. Please choose a different order.');
    }
    // 2. 클라이언트 측 유효성 검사: 이름 중복 확인 (프론트엔드에 로드된 데이터 기준)
    if (optionTypes.filter(type => type.name === optionTypeData.name).length > 0) {
      throw new Error('Option type name already exists. Please choose a different name.');
    }
    console.log("optionTypeService.js: 13", optionTypeData);
    return await axios.post(VITE_ADMIN_TYPE_URL, optionTypeData);
  },
  updateOptionType: (id, optionTypeData) => {
    return axios.put(`${VITE_ADMIN_TYPE_URL}/${id}`, optionTypeData);
  },
  deleteOptionType: (id) => {
    return axios.delete(`${VITE_ADMIN_TYPE_URL}/${id}`);
  },
};

export default optionTypeService;