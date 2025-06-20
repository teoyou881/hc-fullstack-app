package teo.springjwt.product.repository.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import teo.springjwt.product.dto.ResponseProductEntity;

public interface ProductEntityRepositoryCustom {
  Page<ResponseProductEntity> findAllProductsWithMinPriceAndMaxPrice(String name, String skuCode, Pageable pageable);

}
