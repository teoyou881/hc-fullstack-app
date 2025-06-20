import apiClient from '../../util/apiClient.js';

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 계층
 */
const userService = {

  /**
   * 모든 사용자 목록을 조회합니다.
   * @returns {Array} 사용자 객체 배열
   */
  async getAllUsers() {
    try {
      const response = await apiClient.get('/users');
      return response.data;
    } catch (error) {
      console.error('[UserService Error] Error getting all users:', error.message);
      throw new Error('Failed to retrieve users.');
    }
  },

  /**
   * ID로 특정 사용자를 조회합니다.
   * @param {string} userId - 조회할 사용자의 ID
   * @returns {Object|null} 사용자 객체 또는 null
   */
  async getUserById(userId) {
    try {
      const response = await apiClient.get(`/user/${userId}`);
      // 보안을 위해 비밀번호 등 민감 정보는 제거하고 반환하는 것이 좋습니다.
      console.log("userService.js: 32", response.data);
      return response.data;
    } catch (error) {
      console.error(`[UserService Error] Error getting user by ID ${userId}:`, error.message);
      throw new Error('Failed to retrieve user.');
    }
  },

  /**
   * 이메일로 특정 사용자를 조회합니다. (로그인 등에서 사용)
   * @param {string} email - 조회할 사용자의 이메일
   * @returns {Object|null} 사용자 객체 또는 null (비밀번호 포함 가능성 있음, 로그인 시)
   */
  async getUserByEmail(email) {
    try {
      const response = await apiClient.get(`/user/${email}`);
      return response.data || null; // 로그인 시 비밀번호 확인을 위해 전체 객체 반환
    } catch (error) {
      console.error(`[UserService Error] Error getting user by email ${email}:`, error.message);
      throw new Error('Failed to retrieve user by email.');
    }
  },

  /**
   * 새로운 사용자를 생성합니다.
   * 실제 비밀번호는 여기서 해싱되어야 합니다 (bcrypt 등 사용).
   * @param {Object} userData - 생성할 사용자 데이터 (email, password, username 등)
   * @returns {Object} 생성된 사용자 객체 (비밀번호 제외)
   */
  async createUser(userData) {
    try {
      const response = await apiClient.post('/user', userData);
      return response.data;
    } catch (error) {
      console.error('[UserService Error] Error creating user:', error.message);
      throw error; // 에러를 호출자에게 다시 던짐
    }
  },

  /**
   * 기존 사용자를 업데이트합니다.
   * @param {string} userId - 업데이트할 사용자의 ID
   * @param {Object} updateData - 업데이트할 사용자 데이터
   * @returns {Object|null} 업데이트된 사용자 객체 또는 null
   */
  async updateUser(userId, updateData) {
    try {
      const response = await apiClient.put(`/user/${userId}`, updateData);
      return response.data;
    } catch (error) {
      console.error(`[UserService Error] Error updating user ${userId}:`, error.message);
      throw new Error('Failed to update user.');
    }
  },

  /**
   * 사용자를 삭제합니다.
   * @param {string} userId - 삭제할 사용자의 ID
   * @returns {boolean} 삭제 성공 여부
   */
  async deleteUser(userId) {
    try {
      await apiClient.delete(`/user/${userId}`);
      return true;
    } catch (error) {
      console.error(`[UserService Error] Error deleting user ${userId}:`, error.message);
      throw new Error('Failed to delete user.');
    }
  }
};

export default userService;
