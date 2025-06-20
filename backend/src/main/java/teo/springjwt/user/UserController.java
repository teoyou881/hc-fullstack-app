package teo.springjwt.user;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.user.dto.RequestRegisterDTO;
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
  public String user(Principal principal) {
    System.out.println(principal);
    return "admin Controller";
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
