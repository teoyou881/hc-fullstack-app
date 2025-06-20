package teo.springjwt.product.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.product.entity.ImageUrlEntity;
import teo.springjwt.product.entity.ProductColorVariantEntity;
import teo.springjwt.product.entity.SkuEntity;

@Data
@Builder
public class ResponseSkuDTO {
  private Long id;
  private String skuCode; // SkuEntity의 name 필드가 skuCode 역할을 한다고 가정
  private String description;
  private BigDecimal price;
  private String color;
  private int stock;
  private String thumbnailUrl; // 썸네일 이미지 URL
  private List<ResponseImageDto> images; // 모든 이미지 URL 목록 (ResponseImageDto로 변경)
  private List<ResponseSkuOptionValueDTO> optionValues;

  // Entity에서 DTO로 변환
  public static ResponseSkuDTO fromEntity(SkuEntity skuEntity) {

    ProductColorVariantEntity colorVariant = skuEntity.getColorVariant();

    List<ResponseImageDto> images = Collections.emptyList(); // 기본값으로 빈 리스트
    String thumbnailUrl = null;

    if (colorVariant != null) {
      // ProductColorVariantEntity의 이미지 목록을 DTO로 변환
      if (colorVariant.getImages() != null) { // getImages()는 List<ImageUrlEntity>를 반환한다고 가정
        images = colorVariant.getImages().stream()
                             .map(ResponseImageDto::fromEntity)
                             .collect(Collectors.toList());
      }

      // ProductColorVariantEntity에서 썸네일 URL을 찾거나 가져옵니다.
      // ProductColorVariantEntity에 getThumbnailUrl() 메서드가 있다면 사용
      // 없다면 여기서 직접 찾습니다.
      thumbnailUrl = colorVariant.getImages() != null ?
                     colorVariant.getImages().stream()
                                 .filter(ImageUrlEntity::isThumbnail)
                                 .map(ImageUrlEntity::getImageUrl)
                                 .findFirst()
                                 .orElse(null)
                                                      : null;

      // 만약 ProductColorVariantEntity에 이미 썸네일을 가져오는 편의 메서드가 있다면:
      // thumbnailUrl = colorVariant.getThumbnailUrl();
    }


    // 옵션 값들 변환
    List<ResponseSkuOptionValueDTO> optionValues = Collections.emptyList(); // 기본값으로 빈 리스트
    if (skuEntity.getSkuOptionValues() != null) {
      optionValues = skuEntity.getSkuOptionValues().stream()
                              .map(ResponseSkuOptionValueDTO::fromEntity)
                              .collect(Collectors.toList());
    }

    return ResponseSkuDTO.builder()
                         .id(skuEntity.getId())
                         .skuCode(skuEntity.getName()) // SkuEntity의 name 필드가 skuCode 역할을 한다고 가정
                         .description(skuEntity.getDescription())
                         .price(skuEntity.getPrice())
                         .color(colorVariant.getColorName())
                         .stock(skuEntity.getStock())
                         .thumbnailUrl(thumbnailUrl) // ProductColorVariant에서 가져온 썸네일
                         .images(images) // ProductColorVariant에서 가져온 이미지 목록
                         .optionValues(optionValues)
                         .build();
  }
}