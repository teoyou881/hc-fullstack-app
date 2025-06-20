package teo.springjwt.product.repository.group;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

public class OptionGroupEntityRepositoryCustomImpl implements OptionGroupEntityRepositoryCustom{
  private final JPAQueryFactory queryFactory;

  public OptionGroupEntityRepositoryCustomImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

}
