package teo.springjwt.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import teo.springjwt.common.jwt.JWTFilter;
import teo.springjwt.common.jwt.JWTUtil;
import teo.springjwt.common.jwt.LoginFilter;
import teo.springjwt.common.jwt.RefreshTokenService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  // AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
  private final AuthenticationConfiguration authenticationConfiguration;

  private final JWTUtil jwtUtil;

  private final ObjectMapper objectMapper;

  private final CorsConfigurationSource corsConfigurationSource;
  
  private final RefreshTokenService refreshTokenService;

  // 명시적으로 등록해야 한다.
  // security 5.0부터는 명시적으로 passwordEncoder를 빈으로 등록하지 않으면 예외 발생.
  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // AuthenticationManager Bean 등록
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  // ⭐ 1. RoleHierarchyImpl 빈 정의 ⭐
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
        "ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .csrf((auth) -> auth.disable())
        .formLogin((auth) -> auth.disable())
        .httpBasic((auth) -> auth.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource));

    // 경로별 인가 작업
    http.authorizeHttpRequests((auth) -> auth
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers(HttpMethod.POST, "/user").permitAll()
        .requestMatchers("/auth/refresh", "/auth/logout").permitAll() // 토큰 갱신, 로그아웃 허용
        .requestMatchers(HttpMethod.GET, "/user").hasRole("USER")
        .requestMatchers("/user", "/", "/product/**", "/find-password", "/reset-password","/login" ).permitAll()
        .requestMatchers("/admin/**").hasRole("MANAGER")
        .requestMatchers("/user/**").hasRole("USER")
        .anyRequest().authenticated());
        
    http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
    http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, objectMapper, refreshTokenService), UsernamePasswordAuthenticationFilter.class);

    // 세션 설정
    http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }
}