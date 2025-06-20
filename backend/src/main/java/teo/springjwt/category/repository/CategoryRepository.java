package teo.springjwt.category.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teo.springjwt.category.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

  // 특정 이름의 카테고리가 존재하는지 확인
  boolean existsByName(String name);

  List<CategoryEntity> findByParentCategoryIsNull();

  // 특정 부모 카테고리의 자식들 중에서 가장 큰 displayOrder를 찾습니다.
  @Query("SELECT MAX(c.displayOrder) FROM CategoryEntity c WHERE c.parentCategory.id = :parentId")
  Optional<Integer> findMaxDisplayOrderByParentId(Long parentId); // 반환 타입을 Integer로 수정
}
