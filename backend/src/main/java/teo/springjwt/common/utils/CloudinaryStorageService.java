package teo.springjwt.common.utils;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CloudinaryStorageService implements FileStorageService {

  private final Cloudinary cloudinary;


  @Override
  public String saveFile(MultipartFile file, String originalFilename) throws IOException {
    try {
      // Cloudinary에 업로드할 때 사용할 옵션 (선택 사항)
      // public_id: Cloudinary에서 이미지의 고유 식별자. 지정하지 않으면 Cloudinary가 자동으로 생성
      // folder: Cloudinary 내의 폴더 경로 지정
      // resource_type: "auto" 또는 "image", "video", "raw"
      // transformation: 업로드 시 이미지 변환 (예: 크기 조정, 워터마크 등)

      Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                                                      Map.of(
                                                          "resource_type", "auto",
                                                          "folder", "product_sku_images" // Cloudinary 내 'product_sku_images' 폴더에 저장
                                                      ));

      // 업로드 결과에서 URL을 추출
      return (String) uploadResult.get("secure_url"); // HTTPS URL 반환
    } catch (IOException e) {
      throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteFile(String fileUrl) throws IOException {
    try {
      // Cloudinary URL에서 public_id를 추출해야 합니다.
      // 예: https://res.cloudinary.com/your_cloud_name/image/upload/v1234567890/folder/public_id.jpg
      // 일반적으로 "folder/public_id" 부분이 public_id가 됩니다.
      // 이 부분을 추출하는 유틸리티 메서드를 사용하는 것이 좋습니다.

      String publicId = extractPublicIdFromCloudinaryUrl(fileUrl);
      if (publicId == null || publicId.isEmpty()) {
        throw new IllegalArgumentException("Invalid Cloudinary URL or public ID not found: " + fileUrl);
      }

      Map deleteResult = cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
      // 삭제 결과 확인 (예: {"result":"ok"})
      if (!"ok".equals(deleteResult.get("result"))) {
        throw new IOException("Failed to delete image from Cloudinary: " + deleteResult.get("result"));
      }
    } catch (Exception e) {
      throw new IOException("Error deleting image from Cloudinary: " + e.getMessage(), e);
    }
  }

  /**
   * Cloudinary URL에서 public_id를 추출하는 헬퍼 메서드.
   * 이 메서드는 Cloudinary URL 구조에 따라 달라질 수 있으므로, 실제 사용 시 테스트 필요.
   * 예: https://res.cloudinary.com/cloud_name/image/upload/v12345/folder/subfolder/my_image_public_id.jpg
   * 위 URL에서 public_id는 "folder/subfolder/my_image_public_id"가 됩니다.
   */
  private String extractPublicIdFromCloudinaryUrl(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    // "upload/" 또는 "upload/v12345/" 다음에 오는 경로가 public_id가 됩니다.
    int uploadIndex = url.indexOf("/upload/");
    if (uploadIndex == -1) {
      return null;
    }
    String path = url.substring(uploadIndex + "/upload/".length());

    // 버전 정보 (v12345/)가 있다면 제거
    if (path.matches("^v\\d+/.*")) {
      path = path.substring(path.indexOf("/") + 1);
    }

    // 확장자 제거
    int lastDotIndex = path.lastIndexOf(".");
    if (lastDotIndex != -1) {
      path = path.substring(0, lastDotIndex);
    }
    return path;
  }
}