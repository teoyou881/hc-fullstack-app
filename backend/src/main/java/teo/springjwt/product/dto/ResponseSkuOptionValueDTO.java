package teo.springjwt.product.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import teo.springjwt.product.entity.ProductOptionGroupEntity;
import teo.springjwt.product.entity.ProductOptionValueEntity;
import teo.springjwt.product.entity.SkuOptionValueEntity;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseSkuOptionValueDTO {
  private Long id; // SkuOptionValueEntity의 ID
  private Long productOptionValueId; // ProductOptionValueEntity의 ID
  private String optionGroupName; // 옵션 그룹 이름 (예: "색상")
  private String optionValue; // 실제 옵션 값 (예: "빨강", "M")

  @Builder
  public ResponseSkuOptionValueDTO(Long id, Long productOptionValueId, String optionGroupName, String optionValue) {
    this.id = id;
    this.productOptionValueId = productOptionValueId;
    this.optionGroupName = optionGroupName;
    this.optionValue = optionValue;
  }

  // SkuOptionValueEntity로부터 DTO를 생성하는 정적 팩토리 메서드
  public static ResponseSkuOptionValueDTO fromEntity(SkuOptionValueEntity skuOptionValueEntity) {
    // 1. skuOptionValueEntity 자체가 null인지 체크
    if (skuOptionValueEntity == null) {
      return null;
    }

    // 2. ProductOptionValueEntity가 null인지 체크
    ProductOptionValueEntity productOptionValue = skuOptionValueEntity.getProductOptionValue();
    if (productOptionValue == null) {
      // 연관된 ProductOptionValue가 없으면 DTO를 생성할 수 없으므로 null 반환 또는 예외 처리
      return null;
    }

    String optionGroupName = null;
    // 3. ProductOptionGroupEntity가 null인지 체크
    ProductOptionGroupEntity optionGroup = productOptionValue.getProductOptionGroup();
    if (optionGroup != null) {
      // 이제 올바르게 ProductOptionGroupEntity의 이름을 가져옵니다.
      optionGroupName = optionGroup.getOptionGroup().getName(); // <-- 여기가 핵심!
    }

    return ResponseSkuOptionValueDTO.builder()
                                    .id(skuOptionValueEntity.getId())
                                    .productOptionValueId(productOptionValue.getId())
                                    .optionGroupName(optionGroupName)
                                    .optionValue(productOptionValue.getOptionValue().getName())
                                    .build();
  }
}