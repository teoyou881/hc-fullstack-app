package teo.springjwt.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "address_id") // 명시적으로 컬럼명 지정
  private Long id;

  @NotBlank(message = "국가를 입력해주세요")
  @Column(name = "country", nullable = false)
  private String country;

  @NotBlank(message = "도시를 입력해주세요")
  @Column(name = "city", nullable = false)
  private String city;

  @NotBlank(message = "주/도를 입력해주세요")
  @Column(name = "state", nullable = false)
  private String state;

  @NotBlank(message = "주소를 입력해주세요")
  @Column(name = "addr_line_1", nullable = false)
  private String addressLine1;

  @Column(name = "addr_line_2")
  private String addressLine2; // Optional

  @NotBlank(message = "우편번호를 입력해주세요")
  @Column(name = "zip_code", nullable = false)
  private String zipCode;

  @NotNull(message = "기본 주소 여부를 설정해주세요")
  @Column(name = "is_default")
  private Boolean isDefault;

  @NotNull(message = "사용자 정보는 필수입니다")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false) // 외래 키
  private UserEntity user;

  @Builder
  public AddressEntity(String country, String city, String state, String addressLine1, String addressLine2,
      String zipCode, Boolean isDefault, UserEntity user) {
    this.country = country;
    this.city = city;
    this.state = state;
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.zipCode = zipCode;
    this.isDefault = (isDefault != null) ? isDefault : false;
    this.user = user;
  }

  // Business method to update address (consider which fields are updatable)
  public void updateAddress(String country, String city, String state, String addressLine1, String addressLine2,
      String zipCode) {
    if (country != null && !country.trim().isEmpty()) {
      this.country = country;
    }
    if (city != null && !city.trim().isEmpty()) {
      this.city = city;
    }
    if (state != null && !state.trim().isEmpty()) {
      this.state = state;
    }
    if (addressLine1 != null && !addressLine1.trim().isEmpty()) {
      this.addressLine1 = addressLine1;
    }
    this.addressLine2 = addressLine2; // Can be null
    if (zipCode != null && !zipCode.trim().isEmpty()) {
      this.zipCode = zipCode;
    }
  }

  public void setIsDefault(Boolean isDefault) {
    if (isDefault != null) {
      this.isDefault = isDefault;
    }
  }

  // Bidirectional link helper
  public void setUser(UserEntity user) {
    this.user = user;
  }
}
