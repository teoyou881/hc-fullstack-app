package teo.springjwt.category.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{
  private final JPAQueryFactory queryFactory;

  public CategoryRepositoryCustomImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

}
