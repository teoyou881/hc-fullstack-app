package teo.springjwt.common.jwt;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teo.springjwt.user.entity.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  Optional<RefreshTokenEntity> findByTokenAndIsRevokedFalse(String token);

  Optional<RefreshTokenEntity> findByUserAndIsRevokedFalse(UserEntity user);

  @Modifying
  @Query("UPDATE RefreshTokenEntity r SET r.isRevoked = true WHERE r.user = :user")
  void revokeAllByUser(@Param("user") UserEntity user);

  @Modifying
  @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :now")
  void deleteExpiredTokens(@Param("now")
  LocalDateTime now);
}
