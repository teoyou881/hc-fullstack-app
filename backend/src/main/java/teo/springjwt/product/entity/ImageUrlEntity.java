package teo.springjwt.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUrlEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_url_id")
  private Long id;

  @NotNull(message = "이미지 URL은 필수입니다")
  @Column(name = "image_url", nullable = false, length = 1024) // URL 길이에 맞춰 length 늘림
  private String imageUrl;

  @NotNull(message = "이미지 이름은 필수입니다.")
  @Column(length = 255)
  private String originalFileName;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "is_thumbnail", nullable = false)
  private boolean isThumbnail;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_variant_id", nullable = false) // 어떤 ColorVariant에 속하는지 연결
  private ProductColorVariantEntity colorVariant;

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof ImageUrlEntity that)) {
      return false;
    }

    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }


  // Constructor for mandatory fields
  public ImageUrlEntity(String imageUrl, String originalFileName, int displayOrder, boolean isThumbnail, ProductColorVariantEntity colorVariant) {
    this.imageUrl = imageUrl;
    this.originalFileName = originalFileName;
    this.displayOrder = displayOrder;
    this.isThumbnail = isThumbnail;
    this.colorVariant = colorVariant; // ⭐ ProductColorVariantEntity를 받도록 변경
  }

  // Business methods
  public void updateImageUrl(String newImageUrl) {
    if (newImageUrl != null && !newImageUrl.trim().isEmpty()) {
      this.imageUrl = newImageUrl;
    }
  }

  public void updateDisplayOrder(int newOrder) {
    if (newOrder >= 0) {
      this.displayOrder = newOrder;
    }
  }

  public void setAsThumbnail(boolean isThumbnail) {
    this.isThumbnail = isThumbnail;
  }

  // set ColorVariant method
  public void setColorVariant(ProductColorVariantEntity colorVariant) {
    this.colorVariant = colorVariant;
  }
}