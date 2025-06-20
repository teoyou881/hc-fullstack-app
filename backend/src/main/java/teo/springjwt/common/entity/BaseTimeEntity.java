package teo.springjwt.common.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass // JPA 엔티티들이 상속받아 공통 매핑 정보를 재사용할 때 사용
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능 활성화
public abstract class BaseTimeEntity { // 직접 객체로 생성되지 않도록 추상 클래스로 선언

  @CreatedDate // 생성일자를 자동 주입
  @Column(updatable = false) // 생성일자는 업데이트되지 않도록 설정
  private LocalDateTime createdDate;

  @LastModifiedDate // 수정일자를 자동 주입
  private LocalDateTime lastModifiedDate;
}