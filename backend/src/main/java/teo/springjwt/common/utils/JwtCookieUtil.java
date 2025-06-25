package teo.springjwt.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import teo.springjwt.common.jwt.JWTUtil;

@Component
@RequiredArgsConstructor
public class JwtCookieUtil {

  private final JWTUtil jwtUtil;

  public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {

    Cookie accessTokenCookie = new Cookie("Authorization", accessToken);
    accessTokenCookie.setMaxAge((int)jwtUtil.getJwtProperties().getAccessTokenExpirationMs());
    accessTokenCookie.setPath("/");
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);

    Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
    refreshTokenCookie.setMaxAge((int)jwtUtil.getJwtProperties().getRefreshTokenExpirationMs());
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);

    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);
  }

  public static void clearAuthCookies(HttpServletResponse response) {
    Cookie accessTokenCookie = new Cookie("Authorization", null);
    accessTokenCookie.setMaxAge(0);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);

    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
    refreshTokenCookie.setMaxAge(0);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);

    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);
  }

  public static String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public static String extractAccessTokenFromCookies(HttpServletRequest request) {
    return extractTokenFromCookies(request, "Authorization");
  }

  public static String extractRefreshTokenFromCookies(HttpServletRequest request) {
    return extractTokenFromCookies(request, "RefreshToken");
  }
}