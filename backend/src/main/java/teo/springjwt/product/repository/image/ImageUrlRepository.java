package teo.springjwt.product.repository.image;

import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.product.entity.ImageUrlEntity;

public interface ImageUrlRepository extends JpaRepository<ImageUrlEntity, Long> {

}
