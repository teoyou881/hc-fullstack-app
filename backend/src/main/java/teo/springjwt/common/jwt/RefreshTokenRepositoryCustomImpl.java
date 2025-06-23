package teo.springjwt.common.jwt;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import teo.springjwt.user.entity.UserEntity;

public class RefreshTokenRepositoryCustomImpl implements RefreshTokenRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final EntityManager entityManager;

  public RefreshTokenRepositoryCustomImpl(JPAQueryFactory queryFactory, EntityManager entityManager) {
    this.queryFactory = queryFactory;
    this.entityManager = entityManager;
  }


  @Override
  public void revokeAllByUserQuerydsl(UserEntity user) {

  }

  @Override
  public void deleteExpiredTokensQuerydsl(LocalDateTime now) {

  }
}
