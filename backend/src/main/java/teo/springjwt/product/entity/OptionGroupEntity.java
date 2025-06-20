package teo.springjwt.product.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionGroupEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "option_group_id")
  private Long id;

  @NotNull(message = "순서는 필수입니다.")
  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @NotBlank(message = "옵션 이름은 필수입니다")
  @Column(name = "option_name", nullable = false)
  private String name; // 예: "색상", "사이즈", "재질"

  @OneToMany(mappedBy = "optionGroup", cascade = ALL, orphanRemoval = true, fetch = LAZY)
  private List<ProductOptionGroupEntity> productOptionGroups = new ArrayList<>();

  // OptionValue와 1:N 관계 (OptionGroup은 여러 OptionValue를 가짐)
  // cascade = CascadeType.ALL, orphanRemoval = true: OptionGroup 삭제 시 연관된 OptionValue도 함께 삭제
  @OneToMany(mappedBy = "optionGroup", cascade = ALL, orphanRemoval = true, fetch = LAZY)
  private List<OptionValueEntity> optionValues = new ArrayList<>();

  // Constructor
  public OptionGroupEntity(String name,int displayOrder) {
    this.displayOrder = displayOrder;
    this.name = name;
  }

  // Business method to update name
  public void updateName(String newName) {
    if (newName != null && !newName.trim().isEmpty()) {
      this.name = newName;
    }
  }

  // 연관관계 편의 메서드 (양방향 매핑 시)

  public void addOptionValue(OptionValueEntity optionValue) {
    this.optionValues.add(optionValue);
    optionValue.setOptionGroup(this); // OptionValue 엔티티에도 연관관계 설정
  }

  public void removeOptionValue(OptionValueEntity optionValue) {
    if (optionValue != null && this.optionValues.remove(optionValue)) {
      optionValue.setOptionGroup(null);
    }
  }
}