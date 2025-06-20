package teo.springjwt.product.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestProductCreate { // 이름은 주로 ~Request 또는 ~Command
  @NotBlank(message = "상품명은 필수입니다.")
  private String name;

  @NotNull(message = "가격은 필수입니다.")
  @Positive(message = "가격은 0보다 커야 합니다.")
  private BigDecimal price; // 엔티티의 price 필드가 있다면 일치시키고, DTO에서는 최종 가격을 포함하지 않고 기본 가격만 받음

  private String description; // 설명은 선택 사항일 수 있으므로 @NotBlank 제거

  @NotNull(message = "카테고리 ID는 필수입니다.")
  private Long categoryId;

  @NotNull(message = "색상은 필수입니다.")
  private List<String> Colors;

  // 상품에 연결될 옵션 그룹 및 그 하위 옵션 값들
  private List<ProductOptionGroupRequest> optionGroups; // Nullable (옵션이 없는 상품도 있을 수 있으므로)

  // 내부 클래스: ProductOptionGroup 요청 DTO
  @Getter
  @Setter
  @NoArgsConstructor
  public static class ProductOptionGroupRequest {
    @NotNull(message = "옵션 그룹 ID는 필수입니다.")
    private Long id; // 기존 옵션 그룹을 선택하는 경우 필요

    @NotNull(message = "옵션 그룹 이름은 필수입니다.")
    private String name;

    // 하위 옵션 값 리스트
    private List<ProductOptionValueRequest> optionValues;
  }

  // 내부 클래스: ProductOptionValue 요청 DTO
  @Getter
  @Setter
  @NoArgsConstructor
  public static class ProductOptionValueRequest {
    @NotNull(message = "옵션 값 ID는 필수입니다.")
    private Long id;

    @NotBlank(message = "옵션 값 이름은 필수입니다.")
    private String name;

    @NotNull(message = "추가 가격은 필수입니다.")
    private Integer extraPrice;
  }
}