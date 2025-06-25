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
        // 1. 현재 SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
          currentUserEmail = userDetails.getUsername();
            log.info("Logging out user: {}", currentUserEmail);
        }

        /*todo*/
        //Access Token 추출 및 블랙리스트 처리 (권장)
        // String accessToken = JwtCookieUtil.extractAccessTokenFromCookies(request);
        // if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
        //     // Redis나 메모리 캐시에 블랙리스트 추가
        //     tokenBlacklistService.addToBlacklist(accessToken);
        // }

        // 3. 리프레시 토큰 추출 및 무효화 (JwtCookieUtil 사용)
        String refreshToken = JwtCookieUtil.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        // 4. SecurityContext 명시적 초기화 (보안 강화)
        SecurityContextHolder.clearContext();

        // 5. 쿠키 삭제 (JwtCookieUtil 사용)
        JwtCookieUtil.clearAuthCookies(response);

        /*todo*/
        //지금 당장은 구현할 필요가 있나?
        //로그아웃 이벤트 발행 (선택사항)
        // if (currentUserEmail != null) {
        //   try {
        //     applicationEventPublisher.publishEvent(new UserLogoutEvent(this, currentUserEmail));
        //   } catch (Exception eventException) {
        //     log.warn("Failed to publish logout event for user: {}", currentUserEmail, eventException);
        //     // 이벤트 발행 실패해도 로그아웃은 계속 진행
        //   }
        // }

      return ResponseEntity.ok(Map.of(
          "message", "Logout successful",
          "timestamp", System.currentTimeMillis()
      ));

    } catch (Exception e) {
      log.error("Error during logout", e);
      // 오류가 발생해도 SecurityContext는 초기화
      SecurityContextHolder.clearContext();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body(Map.of("error", "Logout failed"));
    }
  }
}
