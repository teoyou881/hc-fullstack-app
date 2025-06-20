package teo.springjwt.category.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // JSON 바인딩을 위한 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자

public class CategoryCreateDTO {
  // @NotBlank(message = "카테고리 이름은 필수입니다.") // 이름은 항상 필요
  private String name;

  // 부모 카테고리가 없는 경우 (최상위) null, 있는 경우 부모의 ID
  private Long parentId;

  // 최상위 카테고리 생성을 위한 팩토리 메서드
  public static CategoryCreateDTO of(String name) {
    return new CategoryCreateDTO(name, null);
  }

  // 자식 카테고리 생성을 위한 팩토리 메서드
  public static CategoryCreateDTO of(String name, Long parentId) {
    return new CategoryCreateDTO(name, parentId);
  }
}