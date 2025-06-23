package teo.springjwt.common.jwt;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teo.springjwt.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JWTUtil jwtUtil;

  public RefreshTokenEntity createRefreshToken(UserEntity user) {
    // 기존 리프레시 토큰이 있다면 무효화
    revokeAllRefreshTokensByUser(user);

    // 새로운 리프레시 토큰 생성
    String token = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now()
                                           .plusSeconds(jwtUtil.getJwtProperties().getRefreshTokenExpirationMs() / 1000);

    RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                                                        .token(token)
                                                        .user(user)
                                                        .expiresAt(expiresAt)
                                                        .build();

    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional(readOnly = true)
  public Optional<RefreshTokenEntity> findByToken(String token) {
    return refreshTokenRepository.findByTokenAndIsRevokedFalse(token);
  }

  public void revokeRefreshToken(String token) {
    findByToken(token).ifPresent(RefreshTokenEntity::revoke);
  }

  public void revokeAllRefreshTokensByUser(UserEntity user) {
    refreshTokenRepository.revokeAllByUser(user);
  }

  public void deleteExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
  }

  public boolean verifyExpiration(RefreshTokenEntity token) {
    if (token.isExpired()) {
      refreshTokenRepository.delete(token);
      return false;
    }
    return true;
  }
}
