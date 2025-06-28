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
    if (requestUri.startsWith("/api/actuator/health") ||
        requestUri.startsWith("/api/login") ||
        requestUri.startsWith("/api/product") ||
        requestUri.startsWith("/api/auth/refresh") ||  // 토큰 갱신 경로 추가
        requestUri.startsWith("/api/auth/logout")) {   // 로그아웃 경로 추가
      filterChain.doFilter(request, response);
      return;
    }

    // 1. 쿠키에서 JWT토큰 가져오기
    String accessToken = JwtCookieUtil.extractAccessTokenFromCookies(request);
    String refreshToken = JwtCookieUtil.extractRefreshTokenFromCookies(request);

    // 2. JWT가 없으면 다음 필터로 진행
    if (accessToken == null) {
      // Refresh Token이 있으면 토큰 갱신을 위한 응답
      if (refreshToken != null) {
        log.debug("No access token but refresh token exists. Requesting token refresh.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Access token missing\",\"code\":\"TOKEN_REFRESH_REQUIRED\",\"message\":\"Please refresh your access token using the refresh token\"}");
      } else {
        // Refresh Token도 없으면 인증 필요
        log.debug("No tokens found for request: {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Authentication required\",\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"Please login to access this resource\"}");
      }
      return;
    }


    // --- JWT 자체 유효성 검증 로직 추가 ---
    try {
      if (!jwtUtil.validateToken(accessToken)) {
        System.out.println("accessToken = " + accessToken);
        sendTokenRefreshResponse(response, refreshToken);
        return;
      }
    } catch (ExpiredJwtException e) {
      // 만료된 토큰에 대한 특별 처리
      System.out.println("token expired (caught in filter): " + e.getMessage());
      sendTokenRefreshResponse(response, refreshToken);
      return;
    } catch (JwtException e) {
      System.out.println("invalid JWT token (caught in filter): " + e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"Invalid token\",\"code\":\"TOKEN_INVALID\"}");
      return;
    }


    //토큰에서 username과 role 획득
    String email = jwtUtil.getEmail(accessToken);
    String roleString = jwtUtil.getRole(accessToken);


    //userEntity를 생성하여 값 set
    UserEntity userEntity = new UserEntity(email, null, UserRole.valueOf(roleString));

    //UserDetails에 회원 정보 객체 담기
    CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

    //스프링 시큐리티 인증 토큰 생성
    Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authToken);

    filterChain.doFilter(request, response);
  }

  /**
   * 토큰 갱신이 필요한 경우의 응답을 보내는 메서드
   */
  private void sendTokenRefreshResponse(HttpServletResponse response, String refreshToken) throws IOException {
    if (refreshToken != null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"Token expired\",\"code\":\"TOKEN_REFRESH_REQUIRED\",\"message\":\"Please refresh your access token using the refresh token\"}");
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"error\":\"Token expired\",\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"Please login again\"}");
    }
  }

}