package teo.springjwt.product.repository.group;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import teo.springjwt.product.entity.OptionGroupEntity;

public interface OptionGroupEntityRepository extends JpaRepository<OptionGroupEntity, Long>, OptionGroupEntityRepositoryCustom{

  boolean existsByName(String name);

  List<OptionGroupEntity> getCategoryById(Long id);

  OptionGroupEntity getOptionGroupEntityById(Long id);
}

