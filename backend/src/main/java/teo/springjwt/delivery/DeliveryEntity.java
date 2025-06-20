package teo.springjwt.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.order.OrderEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @Column(name = "tracking_number", unique = true, nullable = false) // Tracking number is usually unique
    private String trackingNumber;

    @OneToOne(mappedBy = "delivery")
    private OrderEntity order;

    // Constructor
    public DeliveryEntity(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    // Business methods
    public void assignTrackingNumber(String trackingNumber) {
        if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
            this.trackingNumber = trackingNumber;
        }
    }

}
