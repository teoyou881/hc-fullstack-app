package teo.springjwt.common.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.common.jwt.JWTUtil.TokenType;
import teo.springjwt.common.utils.JwtCookieUtil;
import teo.springjwt.user.dto.CustomUserDetails;
import teo.springjwt.user.dto.UserDto;
import teo.springjwt.user.entity.UserEntity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class RefreshController {

  private final RefreshTokenService refreshTokenService;
  private final JWTUtil jwtUtil;
  private final JwtCookieUtil jwtCookieUtil;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final JWTUtil jWTUtil;

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 쿠키에서 리프레시 토큰 추출 (JwtCookieUtil 사용)
      String refreshToken = JwtCookieUtil.extractRefreshTokenFromCookies(request);
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

      // 현재 refresh 토큰 삭제하고, 새로운 refresh 토큰 발급
      RefreshTokenEntity newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
      String newRefreshToken = newRefreshTokenEntity.getToken();

      // JwtCookieUtil을 사용하여 새로운 토큰들을 쿠키에 설정
      JwtCookieUtil jwtCookieUtil1 = new JwtCookieUtil(jWTUtil);
      jwtCookieUtil1.addAuthCookies(response, newAccessToken, newRefreshToken);

      // UserDto 생성
      UserDto userDTO = UserDto.builder()
                       .id( user.getId())
                       .email(user.getEmail())
                       .role(user.getRole().name())
                       .username(user.getUsername())
                       .phoneNumber(user.getPhoneNumber())
                       .build();

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("success", true);
      responseBody.put("user", userDTO);
      responseBody.put("message", "Token refreshed successfully");
      responseBody.put("tokenInfo", Map.of(
          "accessTokenExpiry", jwtUtil.getTokenExpiryTimestamp(TokenType.ACCESS_TOKEN),
          "refreshTokenExpiry", jwtUtil.getTokenExpiryTimestamp(TokenType.REFRESH_TOKEN)
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
