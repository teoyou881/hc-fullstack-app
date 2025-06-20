package teo.springjwt.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  public UserRepositoryCustomImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }
}
