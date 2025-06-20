package teo.springjwt.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Getter
@MappedSuperclass // JPA 엔티티들이 상속받아 공통 매핑 정보를 재사용할 때 사용
// @EntityListeners(AuditingEntityListener.class)는 BaseTimeEntity에 이미 적용되었으므로 여기서는 생략 가능
// 하지만 명시적으로 다시 적어주어도 무방합니다. (보통 BaseTimeEntity에만 둡니다)
public abstract class BaseEntity extends BaseTimeEntity { // BaseTimeEntity를 상속받음

  @CreatedBy // 생성자를 자동 주입
  @Column(updatable = false) // 생성자는 업데이트되지 않도록 설정
  private String createdBy;

  @LastModifiedBy // 수정자를 자동 주입
  private String lastModifiedBy;
}