package teo.springjwt.common.jwt;

import java.time.LocalDateTime;
import teo.springjwt.user.entity.UserEntity;

public interface RefreshTokenRepositoryCustom {
  /**
   * 특정 사용자의 모든 리프레시 토큰을 무효화(revoked)합니다.
   * @param user 무효화할 토큰의 사용자 엔티티
   */
  void revokeAllByUserQuerydsl(UserEntity user);

  /**
   * 만료된 모든 리프레시 토큰을 삭제합니다.
   * @param now 현재 시간 (이 시간보다 이전인 토큰이 삭제됨)
   */
  void deleteExpiredTokensQuerydsl(LocalDateTime now);
}
