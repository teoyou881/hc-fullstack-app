package teo.springjwt.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
  private Long id;
  private String email;
  private String username;
  private String phoneNumber;
  private String role;
}