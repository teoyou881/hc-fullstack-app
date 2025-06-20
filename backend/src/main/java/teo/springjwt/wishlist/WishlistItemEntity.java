package teo.springjwt.wishlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;
import teo.springjwt.product.entity.SkuEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistItemEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wishlist_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wishlist_id", nullable = false)
  private WishlistEntity wishlist;

  // Reference SKU if product has options, otherwise ProductEntity
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sku_id", nullable = false) // Recommended: Link to SKU
  private SkuEntity sku;

  // Optional: dateAdded to wishlist (though BaseTimeEntity handles this)
  // Optional: notes specific to this wishlisted item

  // Constructor
  public WishlistItemEntity(WishlistEntity wishlist, SkuEntity sku) {
    if (wishlist == null) throw new IllegalArgumentException("Wishlist cannot be null.");
    if (sku == null) throw new IllegalArgumentException("SKU cannot be null.");
    this.wishlist = wishlist;
    this.sku = sku;
  }

  // Bidirectional link helper
  public void setWishlist(WishlistEntity wishlist) {
    this.wishlist = wishlist;
  }
}