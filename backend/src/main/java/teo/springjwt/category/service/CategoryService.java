package teo.springjwt.category.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teo.springjwt.category.CategoryEntity;
import teo.springjwt.category.dao.CategoryCreateDTO;
import teo.springjwt.category.dao.CategoryResponseDTO;
import teo.springjwt.category.repository.CategoryRepository;

@Service
@Transactional
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * 계층 구조 (Tree) 형태의 카테고리 목록을 반환합니다.
   * - 최상위 카테고리만 조회하여 시작하고, DTO 변환 시 재귀적으로 자식들을 포함합니다.
   * - 프론트엔드에서 트리 뷰를 렌더링하는 데 적합합니다.
   *
   * @return 계층 구조의 CategoryResponseDTO 리스트
   */
  @Transactional(readOnly = true)
  public List<CategoryResponseDTO> getHierarchicalCategories() {
    // 1. 데이터베이스에서 parentCategory가 null인 최상위 카테고리만 가져옵니다.
    List<CategoryEntity> rootCategories = categoryRepository.findByParentCategoryIsNull();

    // 2. 각 최상위 CategoryEntity를 CategoryResponseDTO.fromEntityTree()를 사용하여 계층 구조 DTO로 변환합니다.
    return rootCategories.stream()
                         .map(CategoryResponseDTO::fromEntityTree) // 계층 DTO 팩토리 메서드 호출 (이 안에서 재귀 발생)
                         .sorted(Comparator.comparingInt(CategoryResponseDTO::getDisplayOrder)) // 필요한 경우 정렬
                         .toList();
  }

  /**
   * @return 평면 리스트 형태의 CategoryResponseDTO 리스트
   */
  @Transactional(readOnly = true)
  public List<CategoryResponseDTO> getFlatListOfAllCategories() {
    // 1. 데이터베이스의 모든 CategoryEntity를 가져옵니다.
    //    이 시점에서 모든 카테고리는 이미 "평면적인" 리스트 형태로 로드됩니다.
    List<CategoryEntity> allCategories = categoryRepository.findAll();

    // ⭐⭐⭐ 중요 수정: flattenCategoryTree 호출 제거 ⭐⭐⭐
    // findAll()로 가져온 리스트는 이미 평면 리스트이므로, 추가적인 평면화 로직은 필요 없습니다.
    // Set<CategoryEntity> uniqueCategories = new HashSet<>(); // findAll()은 중복을 반환하지 않으므로 필요 없음

    // 2. 가져온 CategoryEntity들을 CategoryResponseDTO.fromEntityFlat()을 사용하여 DTO로 변환합니다.
    //    이때 fromEntityFlat 메서드가 childCategories를 빈 리스트로 설정합니다.
    return allCategories.stream() // Stream<CategoryEntity>
                        .map(CategoryResponseDTO::fromEntityFlat) // Stream<CategoryResponseDTO>
                        .sorted(Comparator.comparing(CategoryResponseDTO::getParentId, Comparator.nullsFirst(Comparator.naturalOrder()))
                                          .thenComparing(CategoryResponseDTO::getId)) // 예시: ID 순으로 정렬
                        .toList(); // Java 16+ .toList()
  }

  //create
  public CategoryEntity createCategory(CategoryCreateDTO request) {
    // 중복을 허용한다.
    // same name category is allowed because men and women can have the same name category
    // if (categoryRepository.existsByName(request.getName())) {
    //   throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists.");
    // }

    CategoryEntity parentCategory = null;
    // request.getParentId()가 null이 아닌 경우, 부모 카테고리 조회
    if (request.getParentId() != null) {
      // findById는 Optional을 반환하므로, orElseThrow를 사용하여 부모가 없으면 예외 발생
      parentCategory = categoryRepository.findById(request.getParentId())
                                         .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + request.getParentId()));
    }

    Integer maxDisplayOrder = categoryRepository.findMaxDisplayOrderByParentId(request.getParentId()).orElseGet(() -> 0);

    // CategoryEntity 생성자 사용 (CategoryEntity 코드에 생성자가 정의되어 있어야 함)
    // 1. 최상위 카테고리 생성 (parentCategory가 null)
    // 2. 자식 카테고리 생성 (parentCategory가 존재)
    CategoryEntity newCategory;
    if(parentCategory == null) {
      newCategory = new CategoryEntity(request.getName(),maxDisplayOrder+1);
    }else {
      newCategory= new CategoryEntity(request.getName(),maxDisplayOrder+1, parentCategory);
    }


    System.out.println("newCategory = " + newCategory);

    return categoryRepository.save(newCategory);
  }

  //todo
  public CategoryEntity updateCategory(CategoryEntity category) {
    return null;
  }

  public CategoryEntity deleteCategory(Long id) {
    CategoryEntity category = categoryRepository.findById(id)
                                                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    if (category.getParentCategory() != null) {
      // 이 부분에서 자식 컬렉션을 수정하므로, parentCategory가 Eager 로딩이 아니거나,
      // 해당 트랜잭션 범위 내에서 parentCategory가 이미 로딩되어 있어야 합니다.
      category.getParentCategory().removeChildCategory(category);
    }
    categoryRepository.delete(category);
    return category;
  }
}