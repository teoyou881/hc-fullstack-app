package teo.springjwt.common.utils;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  /**
   * 파일을 저장하고 저장된 파일의 접근 가능한 URL을 반환합니다.
   * @param file 업로드할 MultipartFile 객체
   * @param originalFilename 원본 파일 이름 (확장자 추출 등에 활용)
   * @return 저장된 파일의 URL
   * @throws IOException 파일 저장 중 발생할 수 있는 예외
   */
  String saveFile(MultipartFile file, String originalFilename) throws IOException;

  /**
   * 주어진 URL에 해당하는 파일을 스토리지에서 삭제합니다.
   * @param fileUrl 삭제할 파일의 URL
   * @throws IOException 파일 삭제 중 발생할 수 있는 예외
   */
  void deleteFile(String fileUrl) throws IOException;
}