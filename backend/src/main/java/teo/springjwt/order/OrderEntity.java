package teo.springjwt.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.delivery.DeliveryEntity;
import teo.springjwt.orderitem.OrderItemEntity;
import teo.springjwt.user.entity.AddressEntity;
import teo.springjwt.user.entity.PaymentMethodEntity;
import teo.springjwt.user.entity.UserEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus; // Consider using an Enum for better type safety

    // Shipping address used for this order (snapshot of address at time of order)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private AddressEntity shippingAddress;

    // Payment method used for this order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod; // Or CardEntity if you stick with that

    // 1:1 relationship with Delivery (Order has one Delivery)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "delivery_id") // Order is the owning side for Delivery
    private DeliveryEntity delivery;

    // 1:N relationship with OrderItem (Order has many OrderItems)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // Constructor for creating a new order
    public OrderEntity(UserEntity user, BigDecimal totalPrice, OrderStatus orderStatus, AddressEntity shippingAddress, PaymentMethodEntity paymentMethod) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Total price must be non-negative.");
        if (orderStatus == null) throw new IllegalArgumentException("Order status is required.");
        if (shippingAddress == null) throw new IllegalArgumentException("Shipping address is required.");
        if (paymentMethod == null) throw new IllegalArgumentException("Payment method is required.");

        this.user = user;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Business methods for managing order status and items
    public void updateStatus(OrderStatus newStatus) { // Consider using an Enum for status
        if (newStatus != null) {
            this.orderStatus = newStatus;
        }
    }

    public void addOrderItem(OrderItemEntity orderItem) {
        if (orderItem != null && !this.orderItems.contains(orderItem)) {
            this.orderItems.add(orderItem);
            orderItem.setOrder(this); // Ensure bidirectional link
        }
    }

    public void removeOrderItem(OrderItemEntity orderItem) {
        if (orderItem != null && this.orderItems.remove(orderItem)) {
            orderItem.setOrder(null); // Break bidirectional link
        }
    }

    public void setDelivery(DeliveryEntity delivery) {
        if (delivery != null) {
            this.delivery = delivery;
            // No need for delivery.setOrder(this) if Order is the owning side for @JoinColumn
            // but if Delivery also has an 'order' field, you'd add: delivery.setOrder(this);
        }
    }
}