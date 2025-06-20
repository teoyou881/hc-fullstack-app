package teo.springjwt.product.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option_value") // 조인 테이블의 이름을 명시
public class ProductOptionValueEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_option_value_id")
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "product_option_group_id", nullable = false)
  private ProductOptionGroupEntity productOptionGroup;

  // OptionGroup과의 ManyToOne 관계: 여러 ProductOptionGroup이 하나의 OptionGroup에 속할 수 있음
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "option_value_id", nullable = false)
  private OptionValueEntity optionValue;

  // 추가 속성 (예: 이 옵션 그룹이 상품에서 어떤 순서로 보여질지)
  @Column(name = "display_order", nullable = false)
  private int displayOrder; // 상품 내에서 이 옵션 그룹이 보여지는 순서

  // 추가 속성: 이 옵션 값으로 인한 추가 가격
  @Column(name = "extra_price", nullable = false)
  private BigDecimal extraPrice; // 기본적으로 0 또는 음수도 가능

  @Builder
  public ProductOptionValueEntity(ProductOptionGroupEntity productOptionGroup, OptionValueEntity optionValue, int displayOrder, BigDecimal extraPrice) {
    this.productOptionGroup = productOptionGroup;
    this.optionValue = optionValue;
    this.displayOrder = displayOrder;
    this.extraPrice = extraPrice;
  }

  // 연관관계 편의 메서드 (양방향 매핑 시 필수)
  // ProductOptionGroupEntity에서 이 메서드를 호출하여 양방향 관계를 설정합니다.
  public void setProductOptionGroup(ProductOptionGroupEntity productOptionGroup) {
    this.productOptionGroup = productOptionGroup;
  }

  // (선택 사항) displayOrder 업데이트 메서드
  public void updateDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  // (선택 사항) extraPrice 업데이트 메서드
  public void updateExtraPrice(BigDecimal extraPrice) {
    this.extraPrice = extraPrice;
  }
}

