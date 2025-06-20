package teo.springjwt.cartitem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.cart.CartEntity;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.product.entity.SkuEntity;

@Entity
@Getter
// @Setter // Consider removing
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemEntity extends BaseTimeEntity { // Add BaseTimeEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false) // CartItem must belong to a Cart
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false) // CartItem must reference an SKU
    private SkuEntity sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public CartItemEntity(CartEntity cart, SkuEntity sku, Integer quantity) {
        this.cart = cart;
        this.sku = sku;
        this.quantity = quantity;
    }

    // 수량 업데이트 메서드
    public void updateQuantity(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.quantity = quantity;
        } else {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }

    // 양방향 관계 설정 메서드
    @Column(name = "price_at_add", nullable = false, precision = 10, scale = 2) // Price when added to cart
    private BigDecimal priceAtAdd;

    // Constructor with all mandatory fields
    public CartItemEntity(CartEntity cart, SkuEntity sku, Integer quantity, BigDecimal priceAtAdd) {
        if (cart == null) throw new IllegalArgumentException("Cart cannot be null.");
        if (sku == null) throw new IllegalArgumentException("SKU cannot be null.");
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (priceAtAdd == null || priceAtAdd.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price at add must be non-negative.");

        this.cart = cart;
        this.sku = sku;
        this.quantity = quantity;
        this.priceAtAdd = priceAtAdd;
    }
    // Bidirectional link helper method for the 'mappedBy' side
    public void setCart(CartEntity cart) {
        this.cart = cart;
    }
}
