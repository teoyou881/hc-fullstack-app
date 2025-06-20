package teo.springjwt.cart;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.cartitem.CartItemEntity;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.user.entity.UserEntity;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
// @Setter // Consider removing if business methods handle all changes
public class CartEntity extends BaseTimeEntity { // Add BaseTimeEntity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id should be NOT NULL as a cart belongs to a user
    private UserEntity user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> cartItems = new ArrayList<>();

    @Builder
    public CartEntity(UserEntity user) {
        this.user = user;
    }

    // Business methods for managing cart items
    public void addCartItem(CartItemEntity cartItem) {
        if (cartItem == null) {
            throw new IllegalArgumentException("Cart item cannot be null.");
        }
        // Check if item already exists and update quantity instead of adding duplicate
        boolean found = false;
        for (CartItemEntity existingItem : this.cartItems) {
            if (existingItem.getSku().equals(cartItem.getSku())) {
                existingItem.updateQuantity(existingItem.getQuantity() + cartItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            this.cartItems.add(cartItem);
            cartItem.setCart(this); // Ensure bidirectional link
        }
    }

    public void removeCartItem(CartItemEntity cartItem) {
        if (cartItem != null && this.cartItems.remove(cartItem)) {
            cartItem.setCart(null); // Break bidirectional link
        }
    }

    public void setUser(UserEntity user) {
        if (this.user != null) {
            this.user.setCart(null); // 이전 관계 제거
        }
        this.user = user;
        if (user != null && user.getCart() != this) {
            user.setCart(this); // 새 관계 설정
        }
    }

    // You might also want methods like getTotalPrice, clearCart, etc.
    // public BigDecimal getTotalPrice() {
    //     return this.cartItems.stream()
    //             .map(item -> item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())))
    //             .reduce(BigDecimal.ZERO, BigDecimal::add);
    // }
}
