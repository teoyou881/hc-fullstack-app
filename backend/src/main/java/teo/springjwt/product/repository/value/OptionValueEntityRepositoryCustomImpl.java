package teo.springjwt.product.repository.value;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

public class OptionValueEntityRepositoryCustomImpl implements OptionValueEntityRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  public OptionValueEntityRepositoryCustomImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

}
