package teo.springjwt.product.repository.sku;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import teo.springjwt.product.entity.SkuEntity;

@Repository
public interface SkuRepository extends JpaRepository<SkuEntity, Long> {

  @Query("SELECT s FROM SkuEntity s WHERE s.product.id = :id")
  List<SkuEntity> findBySkuWithProductId(Long id);
}