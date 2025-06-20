package teo.springjwt.user.dto;

import lombok.Data;

@Data
public class RequestLogin {

  private String email;
  private String password;
}
