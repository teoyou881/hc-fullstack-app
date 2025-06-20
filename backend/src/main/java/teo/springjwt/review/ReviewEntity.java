package teo.springjwt.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.orderitem.OrderItemEntity;
import teo.springjwt.product.entity.ProductEntity;
import teo.springjwt.product.entity.SkuEntity;
import teo.springjwt.user.entity.UserEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id") // 명시적으로 컬럼명 지정
    private Long id;

    @NotNull(message = "사용자 정보는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull(message = "상품 정보는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = true) // Optional: Review specific SKU
    private SkuEntity sku;

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 최대 5점까지 가능합니다")
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 평점

    @Column(name = "title", length = 200) // Title can be optional
    private String title;

    @Size(max = 1000, message = "리뷰 내용은 최대 1000자까지 입력 가능합니다")
    @Column(name = "content", length = 1000,nullable = false, columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", unique = true) // One review per order item
    private OrderItemEntity orderItem;

    @Builder
    public ReviewEntity(UserEntity user, ProductEntity product, Integer rating, String content) {
        this.user = user;
        this.product = product;
        this.rating = rating;
        this.content = content;
    }

    // Full constructor
    public ReviewEntity(UserEntity user, ProductEntity product, SkuEntity sku, Integer rating, String title, String content, OrderItemEntity orderItem) {
        if (user == null) throw new IllegalArgumentException("User is required for review.");
        if (product == null) throw new IllegalArgumentException("Product is required for review.");
        if (rating == null || rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("Review content is required.");

        this.user = user;
        this.product = product;
        this.sku = sku; // Nullable
        this.rating = rating;
        this.title = title; // Nullable
        this.content = content;
        this.orderItem = orderItem; // Nullable
    }

    // Business method to update review
    public void updateReview(Integer newRating, String newTitle, String newContent) {
        if (newRating != null && newRating >= 1 && newRating <= 5) {
            this.rating = newRating;
        }
        this.title = newTitle; // Can be null
        if (newContent != null && !newContent.trim().isEmpty()) {
            this.content = newContent;
        }
    }

    // Bidirectional link helper for orderItem
    public void setOrderItem(OrderItemEntity orderItem) {
        this.orderItem = orderItem;
    }
}
