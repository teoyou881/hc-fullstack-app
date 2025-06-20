package teo.springjwt.product.service;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import teo.springjwt.common.utils.CloudinaryStorageService;
import teo.springjwt.product.dto.ResponseImageDto;
import teo.springjwt.product.entity.ImageUrlEntity;
import teo.springjwt.product.entity.ProductColorVariantEntity;
import teo.springjwt.product.repository.image.ImageUrlRepository;
import teo.springjwt.product.repository.product.ProductColorVariantEntityRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {

  private final ImageUrlRepository imageUrlRepository;
  private final ProductColorVariantEntityRepository colorVariantEntityRepository; // ⭐ ProductColorVariantRepository 주입
  private final CloudinaryStorageService fileStorageService;


  /**
   * 이미지를 특정 색상 변형(ProductColorVariant)에 추가합니다.
   *
   * @param colorVariantId 이미지를 추가할 ProductColorVariant의 ID
   * @param imageFile      업로드할 이미지 파일
   * @param isThumbnail    이 이미지를 썸네일로 설정할지 여부
   * @param displayOrder   이미지 표시 순서
   * @return 추가된 이미지의 DTO
   * @throws IOException            파일 업로드 중 발생할 수 있는 예외
   * @throws EntityNotFoundException ProductColorVariant를 찾을 수 없는 경우
   */
  public ResponseImageDto addImageToColorVariant(Long colorVariantId, MultipartFile imageFile, boolean isThumbnail, int displayOrder)
      throws IOException {

    // 1. 원본 파일명 가져오기
    String originalFileName = imageFile.getOriginalFilename();

    // 2. 이미지 파일을 클라우드 스토리지에 업로드하고 URL 반환
    String imageUrl = fileStorageService.saveFile(imageFile, originalFileName);

    // 3. ProductColorVariantEntity 조회
    ProductColorVariantEntity colorVariant = colorVariantEntityRepository.findById(colorVariantId)
                                                                          .orElseThrow(() -> new EntityNotFoundException("ProductColorVariant not found with ID: " + colorVariantId));

    // 4. 썸네일로 설정하는 경우, 해당 ColorVariant의 기존 썸네일 해제
    if (isThumbnail) {
      colorVariant.getImages().forEach(image -> image.setAsThumbnail(false)); // ColorVariant의 이미지 목록에서 썸네일 해제
    }

    // 5. ImageUrlEntity 생성 및 ProductColorVariant에 연결
    // ImageUrlEntity의 생성자가 (imageUrl, originalFileName, displayOrder, isThumbnail, ProductColorVariantEntity colorVariant)를 받도록 수정 필요
    ImageUrlEntity imageEntity = new ImageUrlEntity(imageUrl, originalFileName, displayOrder, isThumbnail, colorVariant);

    // 6. ProductColorVariant에 ImageUrlEntity 추가 (양방향 관계 편의 메서드 호출)
    colorVariant.addImage(imageEntity); // ProductColorVariantEntity에 addImage 메서드가 있다고 가정

    // 7. ImageUrlEntity 저장
    ImageUrlEntity savedImage = imageUrlRepository.save(imageEntity);

    // (Optional) ProductColorVariant Entity 저장 (cascade 설정에 따라 불필요할 수 있으나, 명시적으로 저장하여 변경 감지)
    // productColorVariantRepository.save(colorVariant);

    return ResponseImageDto.fromEntity(savedImage);
  }

  /**
   * 특정 이미지를 색상 변형(ProductColorVariant)에서 제거합니다.
   *
   * @param imageId 제거할 이미지의 ID
   * @return 남은 이미지들의 DTO 리스트
   * @throws EntityNotFoundException 이미지를 찾을 수 없는 경우
   */
  public List<ResponseImageDto> removeImageFromColorVariant(Long imageId) {
    ImageUrlEntity image = imageUrlRepository.findById(imageId)
                                             .orElseThrow(() -> new EntityNotFoundException("Image not found with ID: " + imageId));

    // 1. 해당 이미지의 ProductColorVariant를 가져옵니다.
    ProductColorVariantEntity colorVariant = image.getColorVariant();
    if (colorVariant == null) {
      throw new IllegalStateException("Image is not associated with any ProductColorVariant.");
    }

    // 2. ColorVariant의 이미지 목록에서 해당 이미지를 제거합니다. (양방향 관계 편의 메서드 호출)
    colorVariant.removeImage(image); // ProductColorVariantEntity에 removeImage 메서드가 있다고 가정

    // 3. 클라우드 스토리지에서 실제 이미지 파일 삭제 (선택 사항, 필요시 구현)
    // fileStorageService.deleteFile(image.getImageUrl()); // 이 메서드는 직접 구현해야 합니다.

    // 4. ImageUrlEntity 삭제
    imageUrlRepository.delete(image);

    // 5. 삭제된 이미지가 썸네일이었을 경우, 새로운 썸네일 지정 로직
    String currentThumbnailUrl = colorVariant.getImages().stream()
                                             .filter(ImageUrlEntity::isThumbnail)
                                             .findFirst()
                                             .map(ImageUrlEntity::getImageUrl)
                                             .orElse(null); // String thumbnailUrl = colorVariant.getThumbnailUrl(); 같은 편의 메서드를 만들어도 됨

    if (currentThumbnailUrl == null && !colorVariant.getImages().isEmpty()) {
      // 썸네일이 없고, 남은 이미지가 있다면 가장 ID가 낮은 이미지를 썸네일로 설정
      colorVariant.getImages()
                  .stream()
                  .min(Comparator.comparing(ImageUrlEntity::getId))
                  .ifPresent(colorVariant::setThumbnail); // ProductColorVariantEntity에 setThumbnail 메서드가 있다고 가정
    }

    // (Optional) ProductColorVariant Entity 저장 (cascade 설정에 따라 불필요할 수 있으나, 명시적으로 저장하여 변경 감지)
    // productColorVariantRepository.save(colorVariant);


    // 남은 이미지들의 DTO 리스트 반환
    return colorVariant.getImages().stream().map(ResponseImageDto::fromEntity).toList();
  }

  /**
   * 특정 이미지를 썸네일로 설정합니다.
   *
   * @param imageId 썸네일로 설정할 이미지의 ID
   * @throws EntityNotFoundException 이미지를 찾을 수 없는 경우
   */
  public void setAsThumbnail(Long imageId) {
    ImageUrlEntity image = imageUrlRepository.findById(imageId)
                                             .orElseThrow(() -> new EntityNotFoundException("Image not found with ID: " + imageId));

    ProductColorVariantEntity colorVariant = image.getColorVariant();
    if (colorVariant == null) {
      throw new IllegalStateException("Image is not associated with any ProductColorVariant.");
    }

    // ProductColorVariantEntity의 setThumbnail 메서드를 사용하여 썸네일 설정
    colorVariant.setThumbnail(image); // ProductColorVariantEntity에 setThumbnail 메서드가 있다고 가정

    // (Optional) ProductColorVariant Entity 저장 (cascade 설정에 따라 불필요할 수 있으나, 명시적으로 저장하여 변경 감지)
    colorVariantEntityRepository.save(colorVariant);

    // ImageUrlEntity의 isThumbnail 필드가 변경되었으므로 해당 엔티티도 저장
    imageUrlRepository.save(image);
  }
}