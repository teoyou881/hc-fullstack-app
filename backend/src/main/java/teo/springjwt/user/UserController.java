package teo.springjwt.user;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.user.dto.CustomUserDetails;
import teo.springjwt.user.dto.RequestRegisterDTO;
import teo.springjwt.user.dto.UserDto;
import teo.springjwt.user.entity.UserEntity;
import teo.springjwt.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {

 private final UserService userService;

  @PostMapping("/user")
  public String signUp(@Valid @RequestBody RequestRegisterDTO requestRegisterDTO) {
    userService.signUpProcess(requestRegisterDTO);
    return "ok";
  }

  @GetMapping("/user")
  public ResponseEntity<Object> user(Principal principal) {
    System.out.println(principal);
    CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
    UserEntity user = userDetails.getUser();

    // DTO 변환
    UserDto userDto = UserDto.builder()
                             .id(user.getId())
                             .email(user.getEmail())
                             .username(user.getUsername())
                             .phoneNumber(user.getPhoneNumber())
                             .role(user.getRole().name())
                             .build();

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("success", true);
    responseBody.put("user", userDto);
    responseBody.put("message", "Token refreshed successfully");

    return ResponseEntity.ok(responseBody);
  }

  // 이렇게 조회할 경우, 트랜잭션 범위 밖에서 user를 찾는다.
  // 따라서, 영속성 컨텍스트에서 관리되지 않는 상태. detached.
  // dirty checking 되지 않는다.
  @GetMapping("/user/{id}")
  public String findUser(@PathVariable("id")
  UserEntity user) {
    return user.getUsername();
  }

  @GetMapping("/admin")
  public String admin() {
    return "admin Controller";
  }

  @GetMapping("/manager")
  public String manager() {
    return "manager Controller";
  }
}
