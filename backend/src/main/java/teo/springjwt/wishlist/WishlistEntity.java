package teo.springjwt.wishlist;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.user.entity.UserEntity;

@Entity
@Getter
// @Setter // Consider removing
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistEntity extends BaseTimeEntity { // Add BaseTimeEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Wishlist and Product/SKU relationship should be N:M via a WishlistItemEntity
    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WishlistItemEntity> wishlistItems = new ArrayList<>(); // Use WishlistItemEntity

    // Constructor
    public WishlistEntity(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("Wishlist must be associated with a user.");
        this.user = user;
    }

    // Bidirectional link helper
    public void setUser(UserEntity user) {
        this.user = user;
    }

    // Business methods for managing wishlist items
    public void addWishlistItem(WishlistItemEntity wishlistItem) {
        if (wishlistItem != null && !this.wishlistItems.contains(wishlistItem)) {
            this.wishlistItems.add(wishlistItem);
            wishlistItem.setWishlist(this); // Ensure bidirectional link
        }
    }

    public void removeWishlistItem(WishlistItemEntity wishlistItem) {
        if (wishlistItem != null && this.wishlistItems.remove(wishlistItem)) {
            wishlistItem.setWishlist(null); // Break bidirectional link
        }
    }
}