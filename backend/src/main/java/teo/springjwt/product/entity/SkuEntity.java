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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkuEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "sku_id")
  private Long id;

  @NotNull(message = "상품 정보는 필수입니다")
  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
  @Column(name = "stock", nullable = false)
  private int stock;

  @Column(name = "sku_code", nullable = false, unique = true, length = 255) // unique = true 추가
  private String name; // 이름을 skuCode로 사용하는 경우. 아니면 String skuCode; 필드 추가.

  @Column(nullable = true, length = 500)
  private String description;

  @NotNull(message = "가격은 필수입니다")
  @Min(value = 0, message = "가격은 0 이상이어야 합니다")
  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_variant_id", nullable = false) // 어떤 색상 변형에 속하는지 연결 (핵심!)
  private ProductColorVariantEntity colorVariant;

  @OneToMany(mappedBy = "sku", cascade = ALL, orphanRemoval = true, fetch = LAZY)
  private List<SkuOptionValueEntity> skuOptionValues = new ArrayList<>();

  @Builder
  public SkuEntity(ProductEntity product, BigDecimal price, int stock, String name, String description, ProductColorVariantEntity colorVariant) {
    this.product = product;
    this.price = price;
    this.stock = stock;
    this.name = name; // 이제 이 name이 skuCode 역할을 합니다.
    this.description = description;
    this.colorVariant = colorVariant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SkuEntity)) return false;
    SkuEntity skuEntity = (SkuEntity) o;
    // ⭐ id 대신 skuCode (name 필드)를 기준으로 equals/hashCode를 정의하는 것이 더 실용적일 수 있습니다.
    //    id는 영속성 컨텍스트에 저장되기 전에는 null일 수 있기 때문입니다.
    return Objects.equals(id, skuEntity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  // 연관관계 편의 메서드들
  public void setProduct(ProductEntity product) {
    this.product = product;
  }

  public void addSkuOptionValue(SkuOptionValueEntity skuOptionValue) {
    if (skuOptionValue != null && !this.skuOptionValues.contains(skuOptionValue)) {
      this.skuOptionValues.add(skuOptionValue);
      skuOptionValue.setSku(this);
    }
  }

  // 비즈니스 메서드들
  public void addStock(int quantity) {
    if (quantity > 0) {
      this.stock += quantity;
    }
  }

  public void deductStock(int quantity) {
    if (quantity > 0 && this.stock >= quantity) {
      this.stock -= quantity;
    } else {
      throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + this.stock);
    }
  }

  public void updatePrice(BigDecimal newPrice) {
    if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) >= 0) {
      this.price = newPrice;
    }
  }
}