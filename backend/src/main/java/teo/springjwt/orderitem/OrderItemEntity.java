package teo.springjwt.orderitem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.order.OrderEntity;
import teo.springjwt.product.entity.SkuEntity;
import teo.springjwt.review.ReviewEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order; // The order this item belongs to

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false) // The specific SKU that was ordered
    private SkuEntity sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Quantity of this SKU in the order

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // Price of the SKU at the time of purchase

    // Optional: One-to-one relationship with Review (if a review can be linked to a specific ordered item)
    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ReviewEntity review;

    // Constructor
    public OrderItemEntity(OrderEntity order, SkuEntity sku, Integer quantity, BigDecimal priceAtPurchase) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null.");
        if (sku == null) throw new IllegalArgumentException("SKU cannot be null.");
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (priceAtPurchase == null || priceAtPurchase.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price at purchase must be non-negative.");

        this.order = order;
        this.sku = sku;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // Bidirectional link helper for the 'mappedBy' side
    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public void setReview(ReviewEntity review) {
        this.review = review;
        if (review != null && review.getOrderItem() != this) {
            review.setOrderItem(this);
        }
    }
}