package teo.springjwt.product.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.product.entity.ProductEntity;

public interface ProductEntityRepository extends JpaRepository<ProductEntity, Long>, ProductEntityRepositoryCustom {

}
