package teo.springjwt.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.user.enumerated.UserRole;

@Data
@Builder
public class RequestRegisterDTO {

  @Size(min = 2, max = 100, message = "사용자 이메일은 2자 이상 100자 이하여야 합니다.")
  @NotBlank(message = "사용자 이름은 필수입니다.")
  private String email;
  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 6, message = "비밀번호는 6자 이상 50자 이하여야 합니다.")
  private String password;
  private String username;
  private String phoneNumber;
  private UserRole role;

  static public RequestRegisterDTO of(String email, String password, String username, String phoneNumber,
      UserRole role) {
    return RequestRegisterDTO
        .builder()
        .email(email)
        .password(password)
        .username(username)
        .phoneNumber(phoneNumber)
        .role(role)
        .build();
  }
}
