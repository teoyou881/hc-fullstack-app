package teo.springjwt.product.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.product.dto.ResponseProductEntity;
import teo.springjwt.product.dto.request.RequestProductCreate;
import teo.springjwt.product.service.ProductService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<Page<ResponseProductEntity>> getProduct(
      Pageable pageable,
      // name 파라미터는 필수가 아님, skuCode 파라미터도 필수가 아님
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String skuCode
  ) {
    Page<ResponseProductEntity> allProducts = productService.getAllProductsWithMinPriceAndMaxPrice(name, skuCode, pageable);
    return ResponseEntity.ok(allProducts);
  }

  @PostMapping
  public ResponseEntity<String> createProduct(
      @Valid
      @RequestBody
      RequestProductCreate dto) {
    productService.createProduct(dto);
    return ResponseEntity.ok("Product created successfully");
  }
}
