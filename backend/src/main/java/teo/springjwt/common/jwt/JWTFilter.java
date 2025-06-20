package teo.springjwt.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import teo.springjwt.user.dto.CustomUserDetails;
import teo.springjwt.user.entity.UserEntity;
import teo.springjwt.user.enumerated.UserRole;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

  private final JWTUtil jwtUtil;

  public JWTFilter(JWTUtil jwtUtil) {

    this.jwtUtil = jwtUtil;
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException, IOException {


    // set jwt httpOnly
    /*//request에서 Authorization 헤더를 찾음
    String authorization= request.getHeader("Authorization");

    System.out.println("authorization = " + authorization);

    //Authorization 헤더 검증
    if (authorization == null || !authorization.startsWith("Bearer ")) {

      System.out.println("token null");
      filterChain.doFilter(request, response);

      //조건이 해당되면 메소드 종료 (필수)
      return;
    }

    System.out.println("authorization now");
    //Bearer 부분 제거 후 순수 토큰만 획득
    String token = authorization.split(" ")[1];
*/

    // 특정 경로 (예: /actuator/health, /login 등 permitAll 경로)에 대해서는 필터링을 건너뛰기
    // Spring Security의 permitAll()보다 먼저 필터가 실행되기 때문에 필터 내에서 직접 처리해야 함.
    String requestUri = request.getRequestURI();
    if (requestUri.startsWith("/actuator/health") || requestUri.startsWith("/login") || requestUri.startsWith("/product") /* ... 기타 permitAll 경로 ... */) {
      filterChain.doFilter(request, response);
      return; // 필터 체인 다음으로 넘기고 현재 필터는 여기서 종료
    }

    // 1. 쿠키에서 JWT(Access Token) 가져오기
    String token = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("Authorization")) { // LoginFilter에서 설정한 쿠키 이름
          token = cookie.getValue();
          break;
        }
      }
    }

    // 2. JWT가 없으면 다음 필터로 진행
    if (token == null) {
      log.debug("No JWT cookie found for request: {}", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

    // --- JWT 자체 유효성 검증 로직 추가 (핵심 개선 부분) ---
    try {
      if (!jwtUtil.validateToken(token)) { // validateToken 메서드에서 서명, 구조, 만료 등을 모두 검증
        System.out.println("token validation failed (invalid signature, malformed, or expired)");
        // 이 경우 401 Unauthorized 또는 다른 적절한 HTTP 상태 코드와 메시지를 반환하는 것이 좋습니다.
        // 하지만 현재는 필터 체인을 계속 진행하지 않고 반환.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        return;
      }
    } catch (ExpiredJwtException e) {
      // 만료된 토큰에 대한 특별 처리 (예: Refresh Token 흐름 유도)
      System.out.println("token expired (caught in filter): " + e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      return;
    } catch (JwtException e) { // 그 외 JWT 관련 모든 예외 (Signature, Malformed 등)
      System.out.println("invalid JWT token (caught in filter): " + e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      return;
    }
    // --- JWT 유효성 검증 로직 끝 ---

    //토큰에서 username과 role 획득
    String email = jwtUtil.getEmail(token);

    // jwt에서 enum으로 바로 변환 못함. 그래서 null 에러가 발생.
    /*
    Error extracting role from token: Cannot convert existing claim value of type 'class java.lang.String' to desired type 'class teo.springjwt.user.enumerated.UserRole'. JJWT only converts simple String, Date, Long, Integer, Short and Byte types automatically. Anything more complex is expected to be already converted to your desired type by the JSON Deserializer implementation. You may specify a custom Deserializer for a JwtParser with the desired conversion configuration via the JwtParserBuilder.deserializer() method. See https://github.com/jwtk/jjwt#custom-json-processor for more information. If using Jackson, you can specify custom claim POJO types as described in https://github.com/jwtk/jjwt#json-jackson-custom-types
    * */
    String roleString =jwtUtil.getRole(token);

    //userEntity를 생성하여 값 set
    UserEntity userEntity = new UserEntity(email, null, UserRole.valueOf(roleString));

    //UserDetails에 회원 정보 객체 담기
    CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

    //스프링 시큐리티 인증 토큰 생성
    // jwt를 사용하고 있기 때문에, credential 쪽은 null
    Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

    // 스프링 시큐리티의 인증 정보를 현재 실행 스레드에 등록하는 표준적인 방법
    // 이 자체가 세션을 만든다는 뜻은 아니다.
    SecurityContextHolder.getContext().setAuthentication(authToken);

    filterChain.doFilter(request, response);
  }
}