package teo.springjwt.product.controller.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.product.dto.ResponseSkuDTO;
import teo.springjwt.product.service.SkuService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SkuController {

  private final SkuService skuService;

  @GetMapping("/product/{id}/sku")
  public ResponseEntity<List<ResponseSkuDTO>> getSku(@PathVariable Long id) {
    List<ResponseSkuDTO> responseSkuOptions = skuService.getAllOptionValueWithProductId(id);
    return ResponseEntity.ok(responseSkuOptions);
  }

  @GetMapping("/sku/{skuId}")
  public ResponseEntity<ResponseSkuDTO> getSkuById(@PathVariable Long skuId) {
    ResponseSkuDTO skuById = skuService.getSkuById(skuId);
    return ResponseEntity.ok(skuById);
  }
}
