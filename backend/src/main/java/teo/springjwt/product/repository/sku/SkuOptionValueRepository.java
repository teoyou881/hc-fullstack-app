package teo.springjwt.product.repository.sku;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import teo.springjwt.product.entity.SkuOptionValueEntity;

@Repository
public interface SkuOptionValueRepository extends JpaRepository<SkuOptionValueEntity, Long> {
}
