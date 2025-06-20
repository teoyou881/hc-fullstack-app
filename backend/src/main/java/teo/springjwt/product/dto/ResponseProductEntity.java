package teo.springjwt.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.product.entity.ImageUrlEntity;
import teo.springjwt.product.entity.ProductEntity;

@Data
@Builder
public class ResponseProductEntity {
  private Long id;
  private String name;
  private String description;
  private Long categoryId;
  private String categoryName;
  private String thumbnailUrl;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  // QueryDSL용 생성자
  @QueryProjection
  public ResponseProductEntity(Long id, String name, String description, Long categoryId, String categoryName,
      String thumbnailUrl, BigDecimal minPrice, BigDecimal maxPrice, LocalDateTime createdDate,
      LocalDateTime lastModifiedDate) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.thumbnailUrl = thumbnailUrl;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.createdDate = createdDate;
    this.lastModifiedDate = lastModifiedDate;
  }

  // Entity to DTO 변환 메서드 (ProductColorVariant에서 썸네일 URL 가져오도록 수정)
  public static ResponseProductEntity from(ProductEntity productEntity, BigDecimal minPrice, BigDecimal maxPrice) {
    // ⭐ ProductColorVariantEntity에서 썸네일 이미지 찾기
    String thumbnailUrl = null;
    if (productEntity.getColorVariants() != null) {
      thumbnailUrl = productEntity.getColorVariants().stream()
                                  .flatMap(colorVariant -> colorVariant.getImages() != null ? colorVariant.getImages().stream() : null) // NPE 방지
                                  .filter(ImageUrlEntity::isThumbnail)
                                  .map(ImageUrlEntity::getImageUrl)
                                  .findFirst()
                                  .orElse(null);
    }


    // CategoryEntity가 null일 경우를 대비하여 null 체크 추가
    Long categoryId = null;
    String categoryName = null;
    if (productEntity.getCategory() != null) {
      categoryId = productEntity.getCategory().getId();
      categoryName = productEntity.getCategory().getName();
    }


    return ResponseProductEntity.builder()
                                .id(productEntity.getId())
                                .name(productEntity.getName())
                                .description(productEntity.getDescription())
                                .categoryId(categoryId)
                                .categoryName(categoryName)
                                .thumbnailUrl(thumbnailUrl) // ProductColorVariant에서 가져온 썸네일
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .createdDate(productEntity.getCreatedDate())
                                .lastModifiedDate(productEntity.getLastModifiedDate())
                                .build();
  }

  // Entity to DTO 변환 메서드 (오버로드 - minPrice, maxPrice 없이)
  public static ResponseProductEntity from(ProductEntity productEntity) {
    // ⭐ ProductColorVariantEntity에서 썸네일 이미지 찾기
    String thumbnailUrl = null;
    if (productEntity.getColorVariants() != null) {
      thumbnailUrl = productEntity.getColorVariants().stream()
                                  .flatMap(colorVariant -> colorVariant.getImages() != null ? colorVariant.getImages().stream() : null) // NPE 방지
                                  .filter(ImageUrlEntity::isThumbnail)
                                  .map(ImageUrlEntity::getImageUrl)
                                  .findFirst()
                                  .orElse(null);
    }


    // CategoryEntity가 null일 경우를 대비하여 null 체크 추가
    Long categoryId = null;
    String categoryName = null;
    if (productEntity.getCategory() != null) {
      categoryId = productEntity.getCategory().getId();
      categoryName = productEntity.getCategory().getName();
    }

    return ResponseProductEntity.builder()
                                .id(productEntity.getId())
                                .name(productEntity.getName())
                                .description(productEntity.getDescription())
                                .categoryId(categoryId)
                                .categoryName(categoryName)
                                .thumbnailUrl(thumbnailUrl) // ProductColorVariant에서 가져온 썸네일
                                .createdDate(productEntity.getCreatedDate())
                                .lastModifiedDate(productEntity.getLastModifiedDate())
                                // minPrice, maxPrice는 이 오버로드에서는 null로 설정되거나,
                                // 필요하다면 별도로 계산 로직을 추가해야 함
                                .build();
  }

  // DTO의 필드를 외부에서 직접 설정해야 하는 경우 (Builder 패턴 사용 시 덜 필요)
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void setMinPrice(BigDecimal minPrice) {
    this.minPrice = minPrice;
  }

  public void setMaxPrice(BigDecimal maxPrice) {
    this.maxPrice = maxPrice;
  }
}