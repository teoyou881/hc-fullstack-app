import axios from 'axios';

const VITE_ADMIN_VALUE_URL = import.meta.env.VITE_ADMIN_URL + '/options/value';

const optionValueService = {
  getAllOptionValues: () => {
    return axios.get(VITE_ADMIN_VALUE_URL); // 특정 옵션 타입의 값만 가져오려면 파라미터 추가: axios.get(`${VITE_ADMIN_VALUE_URL}?optionTypeId=${optionTypeId}`)
  },
  getOptionValueById: (id) => {
    return axios.get(`${VITE_ADMIN_VALUE_URL}/${id}`);
  },
  createOptionValue: (optionValueData) => {
    return axios.post(VITE_ADMIN_VALUE_URL, optionValueData);
  },
  updateOptionValue: (id, optionValueData) => {
    return axios.put(`${VITE_ADMIN_VALUE_URL}/${id}`, optionValueData);
  },
  deleteOptionValue: (id) => {
    return axios.delete(`${VITE_ADMIN_VALUE_URL}/${id}`);
  },
};

export default optionValueService;