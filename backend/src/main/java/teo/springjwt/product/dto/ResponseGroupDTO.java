package teo.springjwt.product.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import teo.springjwt.product.entity.OptionGroupEntity;

@Data
@Builder
public class ResponseGroupDTO {
  private Long id;
  private String name;
  private Integer displayOrder; // OptionGroupEntity에 displayOrder가 있다면
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;

  // OptionValueEntity 리스트를 OptionValueResponseDTO 리스트로 변환하여 포함
  private List<ResponseValueDTO> optionValues;

  public static ResponseGroupDTO fromEntity(OptionGroupEntity entity) {
    // null 체크 추가 (만약 optionValues가 null일 수도 있다면)
    List<ResponseValueDTO> convertedOptionValues = (entity.getOptionValues() != null)
                                                   ? entity.getOptionValues().stream()
                                                           .map(ResponseValueDTO::fromEntityFlat) // OptionValueEntity -> ResponseValueDTO
                                                           .toList()
                                                   : List.of(); // 빈 리스트 반환

    return ResponseGroupDTO.builder()
                                 .id(entity.getId())
                                 .name(entity.getName())
                                 .displayOrder(entity.getDisplayOrder()) // 엔티티에 displayOrder가 있다면
                                 .createdDate(entity.getCreatedDate())
                                 .lastModifiedDate(entity.getLastModifiedDate())
                                 .optionValues(convertedOptionValues)
                                 .build();
  }
}