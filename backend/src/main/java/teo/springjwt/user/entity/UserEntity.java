package teo.springjwt.user.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import teo.springjwt.cart.CartEntity;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.order.OrderEntity;
import teo.springjwt.review.ReviewEntity;
import teo.springjwt.user.enumerated.UserRole;
import teo.springjwt.wishlist.WishlistEntity;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id") // 명시적으로 컬럼명 지정
  private Long id;

  @Column(name = "email", unique = true, nullable = false, length = 100)
  private String email;

  // 일단 옵션으로
  @Column(name = "username", length = 100)
  private String username;

  @Size(min = 6, message = "비밀번호는 6자 이상 50자 이하여야 합니다.")
  @Column(name = "password", nullable = false, length = 255) // 비밀번호 컬럼, 필수 (암호화된 비밀번호 저장)
  private String password;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Enumerated(value = STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  // 1:1 매핑: 한 사용자는 하나의 장바구니를 가짐
  @OneToOne(mappedBy = "user", cascade = ALL, orphanRemoval = true)
  private CartEntity cart;

  // 1:1 매핑: 한 사용자는 하나의 위시리스트를 가짐
  @OneToOne(mappedBy = "user", cascade = ALL, orphanRemoval = true)
  private WishlistEntity wishlist;

  // 1:N 매핑: 한 사용자는 여러 주소를 가질 수 있음
  @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
  private List<AddressEntity> addresses = new ArrayList<>();

  // 1:N 매핑: 한 사용자는 여러 결제 수단을 가질 수 있음
  @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
  private List<PaymentMethodEntity> paymentMethods = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true, fetch = LAZY) // Add cascade for order management if desired
  private List<OrderEntity> orders = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true, fetch = LAZY) // Add cascade for review management if desired
  private List<ReviewEntity> reviews = new ArrayList<>();

  // for Register
  public UserEntity(String email, String password, UserRole role, String username, String phoneNumber) {
    this.email = email;
    this.password = password;
    this.role = role;
    this.username = username;
    this.phoneNumber = phoneNumber;
  }

  // for JWTFilter
  public UserEntity(String email, String password, UserRole role) {
    this.email = email;
    this.password = password;
    this.role = role;

  }

  // --- 비즈니스 로직 메서드 (Setter 대신) ---

  public void updateUsername(String newUsername) {
    this.username = newUsername; // Nullable
  }

  public void updatePhoneNumber(String newPhoneNumber) {
    this.phoneNumber = newPhoneNumber; // null 허용
  }

  public void changePassword(String newPassword) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("새 비밀번호는 필수입니다.");
    }
    this.password = newPassword; // 실제 앱에서는 새 비밀번호도 해싱 필요
  }
  public void changeRole(UserRole newRole) {
    if (newRole == null) throw new IllegalArgumentException("New role cannot be null.");
    this.role = newRole;
  }

  // 연관관계 편의 메서드 (양방향 매핑 시 컬렉션 추가/삭제 로직)
  // Association convenience methods are well implemented
  public void addAddress(AddressEntity address) {
    if (address != null && !this.addresses.contains(address)) {
      this.addresses.add(address);
      address.setUser(this);
    }
  }

  public void removeAddress(AddressEntity address) {
    if (address != null && this.addresses.remove(address)) {
      address.setUser(null);
    }
  }

  public void addPaymentMethod(CardEntity payment) { // Use CardEntity or PaymentMethodEntity
    if (payment != null && !this.paymentMethods.contains(payment)) {
      this.paymentMethods.add(payment);
      payment.setUser(this);
    }
  }

  public void removePaymentMethod(CardEntity payment) { // Use CardEntity or PaymentMethodEntity
    if (payment != null && this.paymentMethods.remove(payment)) {
      payment.setUser(null);
    }
  }

  public void setCart(CartEntity cart) {
    if (cart == null) {
      if (this.cart != null) {
        this.cart.setUser(null); // Break old link
      }
      this.cart = null;
    } else {
      if (this.cart != null && this.cart != cart) { // If changing cart, break old link
        this.cart.setUser(null);
      }
      this.cart = cart;
      if (cart.getUser() != this) { // Ensure bidirectional link
        cart.setUser(this);
      }
    }
  }

  public void setWishlist(WishlistEntity wishlist) {
    if (wishlist == null) {
      if (this.wishlist != null) {
        this.wishlist.setUser(null);
      }
      this.wishlist = null;
    } else {
      if (this.wishlist != null && this.wishlist != wishlist) {
        this.wishlist.setUser(null);
      }
      this.wishlist = wishlist;
      if (wishlist.getUser() != this) {
        wishlist.setUser(this);
      }
    }
  }
}

