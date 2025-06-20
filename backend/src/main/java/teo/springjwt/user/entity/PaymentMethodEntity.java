package teo.springjwt.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.user.enumerated.PaymentMethodType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "method_type_discriminator", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PaymentMethodEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id") // 명시적으로 컬럼명 지정
    private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "is_default", nullable = false)
  private Boolean isDefault;

  @Column(name = "nickname", length = 100)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(name = "method_type", nullable = false, length = 50)
  private PaymentMethodType methodType;

  public PaymentMethodEntity(UserEntity user, PaymentMethodType methodType, Boolean isDefault, String nickname) {
    // this.user = user;
    this.user = user;
    this.methodType = methodType;
    this.isDefault = (isDefault != null) ? isDefault : false;
    this.nickname = nickname;
  }

  public void setMethodType(PaymentMethodType methodType) {
    this.methodType = methodType;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }
}
