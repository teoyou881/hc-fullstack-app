package teo.springjwt.product.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.product.entity.ProductColorVariantEntity;

public interface ProductColorVariantEntityRepository extends JpaRepository<ProductColorVariantEntity, Long> {

}
