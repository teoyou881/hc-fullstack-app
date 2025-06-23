package teo.springjwt.common.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.user.entity.UserEntity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j

public class RefreshController {

  private final RefreshTokenService refreshTokenService;
  private final JWTUtil jwtUtil;

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 쿠키에서 리프레시 토큰 추출
      String refreshToken = extractRefreshTokenFromCookies(request);
      if (refreshToken == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Refresh token not found"));
      }

      // 리프레시 토큰 검증
      RefreshTokenEntity tokenEntity = refreshTokenService.findByToken(refreshToken)
                                                          .orElse(null);

      if (tokenEntity == null || !tokenEntity.isValid()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Invalid refresh token"));
      }

      // 새로운 액세스 토큰 생성
      UserEntity user = tokenEntity.getUser();
      String newAccessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());

      // 새로운 액세스 토큰을 쿠키에 설정
      Cookie accessTokenCookie = new Cookie("Authorization", newAccessToken);
      accessTokenCookie.setMaxAge(60 * 60); // 1시간
      accessTokenCookie.setPath("/");
      accessTokenCookie.setHttpOnly(true);
      // accessTokenCookie.setSecure(true); // HTTPS에서만 사용

      response.addCookie(accessTokenCookie);

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("message", "Token refreshed successfully");
      responseBody.put("user", Map.of(
          "email", user.getEmail(),
          "role", user.getRole().name()
      ));

      return ResponseEntity.ok(responseBody);

    } catch (Exception e) {
      log.error("Error refreshing token", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body(Map.of("error", "Token refresh failed"));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 리프레시 토큰 추출 및 무효화
      String refreshToken = extractRefreshTokenFromCookies(request);
      if (refreshToken != null) {
        refreshTokenService.revokeRefreshToken(refreshToken);
      }

      // 쿠키 삭제
      Cookie accessTokenCookie = new Cookie("Authorization", null);
      accessTokenCookie.setMaxAge(0);
      accessTokenCookie.setPath("/");

      Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
      refreshTokenCookie.setMaxAge(0);
      refreshTokenCookie.setPath("/");

      response.addCookie(accessTokenCookie);
      response.addCookie(refreshTokenCookie);

      return ResponseEntity.ok(Map.of("message", "Logout successful"));

    } catch (Exception e) {
      log.error("Error during logout", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body(Map.of("error", "Logout failed"));
    }
  }

  private String extractRefreshTokenFromCookies(HttpServletRequest request) {
    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
                   .filter(cookie -> "RefreshToken".equals(cookie.getName()))
                   .map(Cookie::getValue)
                   .findFirst()
                   .orElse(null);
    }
    return null;
  }
}
