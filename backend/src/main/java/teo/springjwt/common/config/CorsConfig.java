package teo.springjwt.common.config;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Slf4j
public class CorsConfig {

  @Value("${spring.profiles.active:dev}") // 현재 활성화된 프로필 읽어오기 (기본값 dev)
  private String activeProfile;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> allowedOrigins;

    if ("prod".equals(activeProfile)) {
      allowedOrigins = Arrays.asList("https://hyungchul.com", "https://www.hyungchul.com");
      // 운영 환경에 맞는 실제 도메인만 허용
    } else {
      allowedOrigins = Arrays.asList(
          "http://localhost:3000",
          "http://localhost:5173",
          "http://localhost",
          "http://127.0.0.1",
          "http://127.0.0.1:5173"
          // 로컬 개발 환경용
      );
    }
    log.info("CORS Config: Development profile active. Allowed origins: {}", allowedOrigins);
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}