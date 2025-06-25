package teo.springjwt.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import teo.springjwt.common.utils.JwtCookieUtil;
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

    //todo
    // it should be added to security configuration, not here

    // 특정 경로 (예: /actuator/health, /login 등 permitAll 경로)에 대해서는 필터링을 건너뛰기
    String requestUri = request.getRequestURI();
    if (requestUri.startsWith("/actuator/health") || 
        requestUri.startsWith("/login") || 
        requestUri.startsWith("/product") ||
        requestUri.startsWith("/auth/refresh") ||  // 토큰 갱신 경로 추가
        requestUri.startsWith("/auth/logout")) {   // 로그아웃 경로 추가
      filterChain.doFilter(request, response);
      return;
    }

    // 1. 쿠키에서 JWT(Access Token) 가져오기
    String token = JwtCookieUtil.extractAccessTokenFromCookies(request);


    // 2. JWT가 없으면 다음 필터로 진행
    if (token == null) {
      log.debug("No JWT cookie found for request: {}", request.getRequestURI());
      filterChain.doFilter(request, response);
      return;
    }

    // --- JWT 자체 유효성 검증 로직 추가 ---
    try {
      if (!jwtUtil.validateToken(token)) {
        System.out.println("token validation failed (invalid signature, malformed, or expired)");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Token expired\",\"code\":\"TOKEN_EXPIRED\"}");
        return;
      }
    } catch (ExpiredJwtException e) {
      // 만료된 토큰에 대한 특별 처리
      System.out.println("token expired (caught in filter): " + e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"Token expired\",\"code\":\"TOKEN_EXPIRED\"}");
      return;
    } catch (JwtException e) {
      System.out.println("invalid JWT token (caught in filter): " + e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"Invalid token\",\"code\":\"TOKEN_INVALID\"}");
      return;
    }

    //토큰에서 username과 role 획득
    String email = jwtUtil.getEmail(token);
    //새 토큰 저장 및 재시도:
    // 클라이언트는 서버로부터 받은 새로운 Access Token과 Refresh Token을 기존 토큰을 덮어쓰고 안전하게 저장합니다.
    // 이전에 Access Token 만료로 실패했던 원래 API 요청을 새로운 Access Token으로 자동으로 재시도합니다.
    String roleString = jwtUtil.getRole(token);

    //userEntity를 생성하여 값 set
    UserEntity userEntity = new UserEntity(email, null, UserRole.valueOf(roleString));

    //UserDetails에 회원 정보 객체 담기
    CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

    //스프링 시큐리티 인증 토큰 생성
    Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authToken);

    filterChain.doFilter(request, response);
  }
}