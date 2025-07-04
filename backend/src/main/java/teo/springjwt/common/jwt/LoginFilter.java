package teo.springjwt.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import teo.springjwt.common.jwt.JWTUtil.TokenType;
import teo.springjwt.common.utils.JwtCookieUtil;
import teo.springjwt.user.dto.CustomUserDetails;
import teo.springjwt.user.dto.RequestLogin;
import teo.springjwt.user.dto.ResponseLogin;
import teo.springjwt.user.dto.UserDto;
import teo.springjwt.user.entity.UserEntity;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;
  private final ObjectMapper objectMapper;
  private final RefreshTokenService refreshTokenService;
  private final JwtCookieUtil jwtCookieUtil;


  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {

    // username, password 획득
    // json으로 넘어오기 때문에 이렇게 파라미터를 꺼낼 수 없다. --- mapper 사용하자
    // String email = request.getParameter("email");
    // String password = obtainPassword(request);

    String email = null;
    String password = null;

    try {
      // 요청의 Content-Type이 application/json인 경우만 처리하도록 조건 추가 가능
      if (request.getContentType() != null && request.getContentType().contains("application/json")) {
        RequestLogin requestLogin = objectMapper.readValue(request.getInputStream(), RequestLogin.class);
        email = requestLogin.getEmail();
        password = requestLogin.getPassword();
      } else {
        // JSON이 아닌 일반 폼 데이터나 쿼리 파라미터인 경우 기존 방식 사용 (선택 사항)
        email = request.getParameter("email");
        password = obtainPassword(request); // UsernamePasswordAuthenticationFilter의 메소드
      }

    } catch (IOException e) {
      // JSON 파싱 실패 시 예외 처리
      // 실제 애플리케이션에서는 적절한 로깅 및 예외 메시지 전달 필요
      throw new AuthenticationServiceException("Failed to parse authentication request body", e);
    }

    if (email == null) {
      email = "";
    }
    if (password == null) {
      password = "";
    }


    log.info("LoginFilter attemptAuthentication email: {}, password: {}", email, password);
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
    return authenticationManager.authenticate(authToken);
  }

  // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
      Authentication authentication) throws IOException {

    //UserDetailsS
    UserEntity user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

    // 부여받은 role이 2개 이상일 수 있기 때문에,
    // Collection으로 반환.
    // Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    // Collection<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

    // 무조건 하나의 role만 가지고 있따고 가정.
    // 그리고 RoleHirerachy 를 구현해서 2개 이상일 경우를 처리하자.
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    Optional<String> optionalRole = authorities.stream().findFirst() // Optional<GrantedAuthority>를 반환
                                    .map(GrantedAuthority::getAuthority);// Optional<String>으로 변환
    String role = optionalRole.orElseGet(() -> "");

    // Access Token 생성 (짧은 만료 시간)
    String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
    
    // Refresh Token 생성 (긴 만료 시간)
    RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

    JwtCookieUtil jwtCookieUtil1 = new JwtCookieUtil(jwtUtil);
    jwtCookieUtil1.addAuthCookies(response, accessToken, refreshToken.getToken());

    // ResponseLogin 객체를 JSON으로 응답 본문에 추가
    UserDto userDto = UserDto.builder()
                             .id(user.getId())
                             .email(user.getEmail())
                             .username(user.getUsername())
                             .phoneNumber(user.getPhoneNumber())
                             .role(role)
                             .build();
    ResponseLogin responseLogin = new ResponseLogin(userDto);

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("success", true);
    responseBody.put("message", "Login successful");
    responseBody.put("user", responseLogin);
    responseBody.put("tokenInfo", Map.of(
        "accessTokenExpiry", jwtUtil.getTokenExpiryTimestamp(TokenType.ACCESS_TOKEN),
        "refreshTokenExpiry", jwtUtil.getTokenExpiryTimestamp(TokenType.REFRESH_TOKEN)
    ));
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(responseBody));

  }

  // 로그인 실패시 실행하는 메소드
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException failed) {
    // 실패시 401 응답 코드
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}