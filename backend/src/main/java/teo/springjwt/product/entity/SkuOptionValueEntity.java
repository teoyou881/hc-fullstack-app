// SkuOptionValueEntity.java
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter // 편의 메서드를 위해 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sku_option_value")
public class SkuOptionValueEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "sku_option_value_id")
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "sku_id", nullable = false)
  private SkuEntity sku;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "product_option_value_id", nullable = false)
  private ProductOptionValueEntity productOptionValue;

  @Builder
  public SkuOptionValueEntity(SkuEntity sku, ProductOptionValueEntity productOptionValue) {
    this.sku = sku;
    this.productOptionValue = productOptionValue;
  }

  // 연관관계 편의 메서드 (선택 사항)
  public void setSku(SkuEntity sku) {
    this.sku = sku;
  }

  public void setProductOptionValue(ProductOptionValueEntity productOptionValue) {
    this.productOptionValue = productOptionValue;
  }
}