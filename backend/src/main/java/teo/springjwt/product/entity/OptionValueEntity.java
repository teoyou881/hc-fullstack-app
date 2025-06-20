package teo.springjwt.product.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionValueEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "option_value_id")
  private Long id;

  @NotNull(message = "순서는 필수입니다.")
  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @NotBlank(message = "옵션 값은 필수입니다")
  @Column(name = "name", nullable = false) // 'value'는 SQL 키워드일 수 있어 'value_name' 사용
  private String name; // 예: "빨강", "파랑", "S", "M", "면", "울"

  // 이 옵션 값 자체에 대한 추가 가격 (선택 사항)
  // 최종 SKU 가격은 basePrice + 모든 OptionValue의 extraPrice 합산으로 계산됨
  @NotNull(message = "추가 가격 정보는 필수입니다")
  @Column(name = "extra_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal extraPrice = new BigDecimal("0");

  // 연관관계 편의 메서드 (양방향 매핑 시)
  // OptionGroup과 ManyToOne 관계 (OptionValue는 특정 OptionGroup에 속함)

  @NotNull(message = "옵션 그룹 정보는 필수입니다")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "option_group_id", nullable = false) // 외래 키 컬럼명
  private OptionGroupEntity optionGroup; // 연관관계의 주인

  @OneToMany(mappedBy = "optionValue", cascade = ALL, orphanRemoval = true, fetch = LAZY)
  private List<ProductOptionValueEntity> productOptionValues = new ArrayList<>();

  @Builder
  public OptionValueEntity(String valueName, BigDecimal extraPrice, OptionGroupEntity optionGroup, int displayOrder) {
    this.name = valueName;
    this.extraPrice = extraPrice;
    this.optionGroup = optionGroup;
    this.displayOrder = displayOrder;
  }
  @Builder
  public OptionValueEntity(String valueName,OptionGroupEntity optionGroup,int displayOrder) {
    this.name = valueName;
    this.displayOrder = displayOrder;
    this.optionGroup = optionGroup;
  }

  // Business method to update value or extra price
  public void updateOptionValue(String newValueName, BigDecimal newExtraPrice) {
    if (newValueName != null && !newValueName.trim().isEmpty()) {
      this.name = newValueName;
    }
    if (newExtraPrice != null && newExtraPrice.compareTo(BigDecimal.ZERO) >= 0) {
      this.extraPrice = newExtraPrice;
    }
  }

  // This specific setter is common for bidirectional relationships
  public void setOptionGroup(OptionGroupEntity optionGroup) {
    this.optionGroup = optionGroup;
  }
}