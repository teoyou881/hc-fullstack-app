package teo.springjwt.product.repository.value;

import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.product.entity.OptionValueEntity;

public interface OptionValueEntityRepository extends JpaRepository<OptionValueEntity, Long>,
    OptionValueEntityRepositoryCustom {

  boolean existsByName(String name
  );
}
