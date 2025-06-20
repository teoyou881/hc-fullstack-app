package teo.springjwt.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.product.entity.OptionValueEntity;

@Data
@Builder
public class ResponseValueDTO {
  private Long id;
  private String name; // 또는 value (엔티티 필드명에 따라)
  private BigDecimal extraPrice;
  private Integer displayOrder; // OptionValueEntity에 displayOrder가 있다면
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  // ⭐⭐ 중요: optionGroup 전체 객체를 포함하는 대신, ID만 포함하거나 아예 포함하지 않습니다. ⭐⭐
  private Long optionGroupId; // OptionGroup의 ID만 노출
  private String optionGroupName; // 필요하다면 이름도 노출

  // OptionGroupDetailsResponseDTO fromEntityFlat (깊이 제한)
  public static ResponseValueDTO fromEntityFlat(OptionValueEntity entity) {
    return ResponseValueDTO.builder()
                                 .id(entity.getId())
                                 .name(entity.getName()) // OptionValueEntity의 'value' 필드를 'name'으로 DTO에 매핑 가정
                                 .displayOrder(entity.getDisplayOrder()) // 엔티티에 displayOrder가 있다면
                                 .createdDate(entity.getCreatedDate())
                                 .lastModifiedDate(entity.getLastModifiedDate())
                                 .optionGroupId(entity.getOptionGroup() != null ? entity.getOptionGroup().getId() : null) // null 체크
                                 .optionGroupName(entity.getOptionGroup() != null ? entity.getOptionGroup().getName() : null)
                                 .build();
  }
}