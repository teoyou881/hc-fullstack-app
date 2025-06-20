package teo.springjwt.product.dto; // DTO 패키지에 맞게 조정하세요.

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.product.entity.ImageUrlEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseImageDto {
  private Long id; // image_url_id
  private String imageUrl; // 이미지 URL
  private String originalFileName; // 원본 파일명 (사용자 친화적인 이름)
  private int displayOrder; // 표시 순서
  private boolean isThumbnail; // 썸네일 여부

  // ⭐ skuId 필드를 제거합니다.
  // private Long skuId;

  // ⭐ ProductColorVariantEntity의 ID를 추가합니다.
  private Long colorVariantId;


  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  /**
   * ImageUrlEntity로부터 ResponseImageDto를 생성하는 팩토리 메서드.
   * ProductColorVariant 정보를 포함합니다.
   *
   * @param entity 변환할 ImageUrlEntity 객체
   * @return 생성된 ResponseImageDto 객체
   */
  public static ResponseImageDto fromEntity(ImageUrlEntity entity) {
    if (entity == null) {
      return null;
    }
    // ⭐ skuId 관련 로직을 제거하고, colorVariantId 관련 로직으로 대체합니다.
    Long colorVariantId = null;
    if (entity.getColorVariant() != null) {
      colorVariantId = entity.getColorVariant().getId();
    }

    return ResponseImageDto.builder()
                           .id(entity.getId())
                           .imageUrl(entity.getImageUrl())
                           .originalFileName(entity.getOriginalFileName())
                           .displayOrder(entity.getDisplayOrder())
                           .isThumbnail(entity.isThumbnail())
                           .colorVariantId(colorVariantId) // ⭐ colorVariantId를 설정합니다.
                           .createdDate(entity.getCreatedDate())
                           .lastModifiedDate(entity.getLastModifiedDate())
                           .build();
  }
}