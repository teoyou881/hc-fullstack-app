package teo.springjwt.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.user.enumerated.PaymentMethodType;

@Entity
@Getter
@DiscriminatorValue("CARD")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class CardEntity extends PaymentMethodEntity {

  // 토큰화된 카드 정보 (필수, 비어있으면 안됨, 최소/최대 길이 제한)
  @NotBlank(message = "카드 토큰은 필수입니다.")
  @Size(min = 5, max = 100, message = "카드 토큰은 5자 이상 100자 이하이어야 합니다.")
  @Column(name = "card_token", nullable = false, length = 100)
  private String cardToken;

  // 카드 브랜드 (Visa, MasterCard 등)
  @Size(max = 30, message = "카드 브랜드는 30자 이하로 입력해주세요.")
  @Column(name = "card_brand", length = 30)
  private String cardBrand;

  // 마지막 4자리
  @Size(min = 4, max = 4, message = "카드 마지막 네 자리를 정확히 입력해주세요.")
  @Column(name = "last4", length = 4)
  private String last4;

  // 월: 1~12
  @Min(value = 1, message = "만료 월은 1월 이상이어야 합니다.")
  @Max(value = 12, message = "만료 월은 12월 이하여야 합니다.")
  @Column(name = "exp_month")
  private Integer expMonth;

  // 연도: 2024 이상
  @Min(value = 2024, message = "만료 연도는 2024년 이후여야 합니다.")
  @Column(name = "exp_year")
  private Integer expYear;

  // // Constructors
  // public CardEntity() {
  //   super(); // PaymentMethod의 기본 생성자 호출
  //   setMethodType(PaymentMethodType.CARD); // 타입 설정
  // }

  public CardEntity(String cardToken, String cardBrand, String last4, Integer expMonth, Integer expYear, UserEntity user, Boolean isDefault, String nickname) {
    super(user, PaymentMethodType.CARD, isDefault, nickname); // Call parent constructor
    this.cardToken = cardToken;
    this.cardBrand = cardBrand;
    this.last4 = last4;
    this.expMonth = expMonth;
    this.expYear = expYear;
  }

}
