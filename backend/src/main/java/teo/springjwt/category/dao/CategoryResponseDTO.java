package teo.springjwt.category.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.category.CategoryEntity;

@Data
@Builder
public class CategoryResponseDTO {
  private Long id;
  private String name;
  private Long parentId; // 부모 카테고리 ID
  private String parentName; // 부모 카테고리 이름 (프론트에서 보여주기 위해)
  private int displayOrder;
  private List<CategoryResponseDTO> childCategories;
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  // 1. 계층 구조 (Tree) 생성을 위한 static 팩토리 메서드
  public static CategoryResponseDTO fromEntityTree(CategoryEntity category) {
    CategoryResponseDTO dto = getBuild(category);

    if (category.getParentCategory() != null) {
      dto.setParentId(category.getParentCategory().getId());
      dto.setParentName(category.getParentCategory().getName());
    }

    // 자식 카테고리 재귀 변환
    if (category.getChildCategories() != null && !category.getChildCategories().isEmpty()) {
      dto.setChildCategories(category.getChildCategories().stream()
                                     .map(CategoryResponseDTO::fromEntityTree) // 재귀 호출
                                     .collect(Collectors.toList()));
    } else {
      dto.setChildCategories(new ArrayList<>()); // 빈 리스트로 초기화
    }
    return dto;
  }

  // 2. 평면 리스트 (Flat List) 생성을 위한 static 팩토리 메서드 추가
  // 이 메서드는 childCategories 필드를 항상 빈 리스트로 설정합니다.
  public static CategoryResponseDTO fromEntityFlat(CategoryEntity category) {
    CategoryResponseDTO dto = getBuild(category);

    if (category.getParentCategory() != null) {
      dto.setParentId(category.getParentCategory().getId());
      dto.setParentName(category.getParentCategory().getName());
    }
    // Flat DTO에서는 childCategories 필드를 항상 비웁니다.
    dto.setChildCategories(new ArrayList<>());

    return dto;
  }

  private static CategoryResponseDTO getBuild(CategoryEntity category) {
    return CategoryResponseDTO
        .builder()
        .id(category.getId())
        .name(category.getName())
        .displayOrder(category.getDisplayOrder())
        .createdDate(category.getCreatedDate())
        .lastModifiedDate(category.getLastModifiedDate())
        .build();
  }
}