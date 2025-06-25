package teo.springjwt.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtCookieUtil {

  public static void addAuthCookies(HttpServletResponse response, String accessToken, int accessTokenMaxAge,
      String refreshToken, int refreshTokenMaxAge) {

    Cookie accessTokenCookie = new Cookie("Authorization", accessToken);
    accessTokenCookie.setMaxAge(accessTokenMaxAge);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);

    Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
    refreshTokenCookie.setMaxAge(refreshTokenMaxAge);
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
