package teo.springjwt.product.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductColorVariantEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 색상 변형 고유 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false) // ProductEntity의 ID를 참조
  private ProductEntity product; // 어떤 제품의 색상 변형인지 연결

  @NotNull(message = "색상은 필수입니다.")
  @Column(nullable = false, length = 50)
  private String colorName; // 색상명 (예: White, Black, Red)

  @OneToMany(mappedBy = "colorVariant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("displayOrder ASC") // displayOrder 필드를 기준으로 정렬
  private List<ImageUrlEntity> images = new ArrayList<>(); // ImageUrlEntity 리스트로 변경

  public ProductColorVariantEntity(ProductEntity product, String colorName) {
    this.product = product;
    this.colorName = colorName;
  }

  // 편의메서드
  public void setThumbnail(ImageUrlEntity image) {
    if(image!=null) {
     image.setAsThumbnail(true);
    }
  }

  public void addImage(ImageUrlEntity imageEntity) {
    if (imageEntity != null && !this.images.contains(imageEntity)) {
      this.images.add(imageEntity);
      imageEntity.setColorVariant(this);
    }
  }

  public void removeImage(ImageUrlEntity image) {
    if (image != null && this.images.contains(image)) {
      this.images.remove(image);
      image.setColorVariant(null);
    }
  }

}
