package teo.springjwt.product.controller.admin;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import teo.springjwt.product.dto.ResponseImageDto;
import teo.springjwt.product.service.ImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/sku")
public class ImageController {

  private final ImageService imageService;

  @PostMapping("/addImage")
  public ResponseEntity<?> uploadSingleFile(
      Long skuId,
      @RequestParam("file") MultipartFile imageFile,
      boolean isThumbnail,
      int displayOrder
  ) {
    if (imageFile.isEmpty()) {
      return ResponseEntity.badRequest().body("이미지 파일을 선택해주세요.");
    }
    try {
      ResponseImageDto uploadedImage = imageService.addImageToColorVariant(skuId, imageFile, isThumbnail, displayOrder);
      return new ResponseEntity<ResponseImageDto>(uploadedImage, HttpStatus.CREATED); // 201 Created
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 유효성 검사 실패 시
    } catch (Exception e) {
      // 로깅: e.getMessage()
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{skuId}/updateThumbnail")
  public ResponseEntity<String> updateThumbnailBySkuId(
     @PathVariable Long skuId,
      @RequestBody Map<String, Object> requestMap
  ){
    if (!requestMap.containsKey("imageId")) {
      return ResponseEntity.badRequest().body("imageId is required in the request body.");
    }
    Long imageId;
    try {
      // Map에서 값을 가져올 때는 Object 타입이므로 명시적인 형변환이 필요합니다.
      // 숫자가 JSON에서 Long으로 오는지, Integer로 오는지 확인해야 합니다.
      // Axios는 기본적으로 숫자를 JavaScript Number로 보내고, Java에서는 Long이나 Integer로 매핑될 수 있습니다.
      // 안전하게 Long으로 변환하려면 String으로 받은 후 Long.parseLong()을 사용하는 것도 방법입니다.
      Object imageIdObj = requestMap.get("imageId");
      if (imageIdObj instanceof Integer) { // JSON 숫자가 Integer로 매핑된 경우
        imageId = ((Integer) imageIdObj).longValue();
      } else if (imageIdObj instanceof Long) { // JSON 숫자가 Long으로 매핑된 경우
        imageId = (Long) imageIdObj;
      } else if (imageIdObj instanceof String) { // 만약 문자열로 온다면 파싱
        imageId = Long.parseLong((String) imageIdObj);
      } else {
        return ResponseEntity.badRequest().body("Invalid imageId type.");
      }
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest().body("Invalid imageId format.");
    }

    imageService.setAsThumbnail(imageId);
    return ResponseEntity.ok("Thumbnail updated successfully");
  }

  @DeleteMapping("/images/{imageId}")
  public ResponseEntity<List<ResponseImageDto>> deleteSingleFile(
    @PathVariable Long imageId
  ){
    return ResponseEntity.ok( imageService.removeImageFromColorVariant(imageId));
  }
}
