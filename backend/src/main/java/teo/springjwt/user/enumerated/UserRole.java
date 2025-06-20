package teo.springjwt.user.enumerated;

//spring security 관례에 맞게, enum 클래스 UserRole String 변경
public enum UserRole {
  ROLE_USER,  // 사용자 역할
  ROLE_ADMIN, // 관리자 역할
  ROLE_MANAGER,  // 매니저 역할
  ROLE_GUEST
}
