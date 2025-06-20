package teo.springjwt.category;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teo.springjwt.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryEntity extends BaseTimeEntity { // 생성, 수정 시간 관리를 위해 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // 카테고리 이름 (예: "의류", "상의", "하의")

    @Column(nullable = false)
    private int displayOrder; // 같은 부모를 가진 형제 카테고리 내에서의 정렬 순서

    // --- 계층형 구조를 위한 Self-Referencing 매핑 ---

    // 1. 부모 카테고리 (N:1 관계: 여러 자식 카테고리가 하나의 부모를 가짐)
    @ManyToOne(fetch = LAZY) // 지연 로딩: 부모 카테고리 정보가 필요할 때만 로드
    @JoinColumn(name = "parent_id") // 외래 키 컬럼명: 이 카테고리의 부모 카테고리 ID를 저장
    // nullable = true: 최상위 카테고리(루트)는 부모가 없으므로 null을 허용
    private CategoryEntity parentCategory;

    // 2. 자식 카테고리 목록 (1:N 관계: 하나의 부모 카테고리가 여러 자식을 가짐)
    // mappedBy = "parentCategory": parentCategory 필드가 연관관계의 주인임을 명시
    // cascade = ALL, orphanRemoval = true: 부모 카테고리 삭제 시 자식 카테고리도 함께 삭제될지 여부.
    // 주의: 실제 비즈니스 로직에 따라 cascade 설정은 신중하게 고려해야 합니다.
    //       보통은 부모 삭제 시 자식도 함께 삭제하기보다, 자식 카테고리의 parent_id를 null로 설정하거나 이동시키는 경우가 많습니다.
    //       여기서는 예시를 위해 ALL을 사용하지만, 운영 시에는 변경될 수 있습니다.
    @OneToMany(mappedBy = "parentCategory", cascade = ALL, orphanRemoval = true, fetch = LAZY)
    private List<CategoryEntity> childCategories = new ArrayList<>();

    // ⭐⭐⭐ 핵심: equals()와 hashCode() 구현 시 연관관계 필드 (특히 컬렉션)를 포함하지 않습니다. ⭐⭐⭐
    // 일반적으로 id만 사용하는 것이 안전합니다.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // JPA 프록시를 고려하여 getClass() 대신 instanceof 사용
        if (!(o instanceof CategoryEntity)) return false;
        CategoryEntity that = (CategoryEntity) o;
        // id가 null일 수 있는 경우 (저장 전)와 영속화된 경우를 모두 고려
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0; // id가 null일 경우 0 반환 (저장 전)
    }

    // --- 생성자 ---
    // 최상위 카테고리 생성자 (부모 없음)
    public CategoryEntity(String name, int displayOrder) {
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
        }
        if(displayOrder < 0 || displayOrder > 999999999) {
            throw new IllegalArgumentException("정렬 순서는 필수입니다.");
        }
        this.name = name;
        this.displayOrder = displayOrder;
    }

    // 자식 카테고리 생성자 (부모 지정)
    public CategoryEntity(String name, int displayOrder, CategoryEntity parentCategory) {
        this(name,displayOrder); // 기존 생성자 호출
        if (parentCategory == null) {
            throw new IllegalArgumentException("부모 카테고리는 null이 될 수 없습니다. 최상위 카테고리 생성자를 사용하세요.");
        }
        changeParentCategory(parentCategory); // 연관관계 편의 메서드 사용
    }

    // --- 비즈니스 메서드 (Setter 대신) ---

    // 카테고리 이름 변경
    public void updateName(String newName) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
    }

    // 부모 카테고리 설정 (연관관계 편의 메서드)
    // 기존 부모와의 관계를 끊고 새로운 부모와 연결
    public void changeParentCategory(CategoryEntity newParent) {
        // 기존 부모의 자식 목록에서 자신을 제거
        if (this.parentCategory != null) {
            this.parentCategory.removeChildCategory(this);
        }
        this.parentCategory = newParent;
        // 새로운 부모의 자식 목록에 자신을 추가
        if (newParent != null) {
            newParent.addChildCategory(this);
        }
    }

    // 자식 카테고리 추가 (연관관계 편의 메서드)
    // 양방향 매핑 시 연관관계의 주인이 아닌 쪽에서 사용
    public void addChildCategory(CategoryEntity childCategory) {
        if (childCategory != null && !this.childCategories.contains(childCategory)) {
            this.childCategories.add(childCategory);
            // 자식 카테고리의 부모도 함께 설정 (연관관계의 주인)
            if (childCategory.getParentCategory() != this) {
                childCategory.changeParentCategory(this);
            }
        }
    }

    // 자식 카테고리 제거 (연관관계 편의 메서드)
    public void removeChildCategory(CategoryEntity childCategory) {
        if (childCategory != null && this.childCategories.remove(childCategory)) {
            if (childCategory.getParentCategory() == this) {
                childCategory.changeParentCategory(null); // 자식의 부모 연결 해제
            }
        }
    }
}