package teo.springjwt;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableJpaAuditing
public class SpringJwtApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringJwtApplication.class, args);
  }

  @Bean
  public AuditorAware<String> auditorProvider(){
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null || !authentication.isAuthenticated() ||
          authentication instanceof AnonymousAuthenticationToken) {
        return Optional.empty();
      }

      // JWT 토큰에서 추출한 사용자 이름 또는 ID를 반환
      return Optional.ofNullable(authentication.getName());
    };
  }
}
