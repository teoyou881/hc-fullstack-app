package teo.springjwt.common.jwt;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
// application.yml의 'jwt' 아래 속성들을 매핑
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JWTProperties {
  private String secret;
  private long accessTokenExpirationMs;
  private long refreshTokenExpirationMs;
  private String tokenPrefix;
  private String headerString;
}
