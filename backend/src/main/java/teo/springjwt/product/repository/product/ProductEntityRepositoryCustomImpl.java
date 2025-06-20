package teo.springjwt.product.repository.product;

import static teo.springjwt.category.QCategoryEntity.categoryEntity;
import static teo.springjwt.product.entity.QImageUrlEntity.imageUrlEntity;
import static teo.springjwt.product.entity.QProductColorVariantEntity.productColorVariantEntity;
import static teo.springjwt.product.entity.QProductEntity.productEntity;
import static teo.springjwt.product.entity.QSkuEntity.skuEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import teo.springjwt.product.dto.QResponseProductEntity;
import teo.springjwt.product.dto.ResponseProductEntity;

public class ProductEntityRepositoryCustomImpl implements ProductEntityRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  public ProductEntityRepositoryCustomImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public Page<ResponseProductEntity> findAllProductsWithMinPriceAndMaxPrice(String name, String skuCode,
      Pageable pageable) {
    // 메인 쿼리: ResponseProductEntity로 프로젝션
    JPAQuery<ResponseProductEntity> query = queryFactory
        .select(new QResponseProductEntity(
            productEntity.id,
            productEntity.name,
            productEntity.description,
            categoryEntity.id,
            categoryEntity.name,
            // 썸네일 URL은 여기서 직접 가져오기 어렵기 때문에 null로 설정하고 나중에 채웁니다.
            // 복잡한 쿼리를 방지하고 성능을 위해 보통 이렇게 분리합니다.
            Expressions.nullExpression(String.class), // ⭐ nullExpression에 타입 지정 (String.class)
            skuEntity.price.min(), // SKU 최소 가격 (Product에 속한 모든 SKU 중 최소)
            skuEntity.price.max(), // SKU 최대 가격 (Product에 속한 모든 SKU 중 최대)
            productEntity.createdDate,
            productEntity.lastModifiedDate
        ))
        .from(productEntity)
        .leftJoin(productEntity.category, categoryEntity)
        .leftJoin(productEntity.skus, skuEntity) // SKU의 min/max price를 위해 SkuEntity 조인 유지
        .groupBy(
            productEntity.id,
            productEntity.name,
            productEntity.description,
            categoryEntity.id,
            categoryEntity.name,
            productEntity.createdDate,
            productEntity.lastModifiedDate
        );

    // 검색 조건 동적으로 추가
    BooleanBuilder whereCondition = new BooleanBuilder();

    if (StringUtils.hasText(name)) {
      whereCondition.and(productEntity.name.lower().like("%" + name.toLowerCase() + "%"));
    }

    if (StringUtils.hasText(skuCode)) {
      // SKU 코드 검색 조건: 해당 SKU 코드를 가진 SKU가 Product에 속하는지 확인
      whereCondition.and(
          JPAExpressions.selectOne()
                        .from(skuEntity)
                        .where(skuEntity.product.eq(productEntity) // 해당 productEntity에 속하는 SKU만
                                                .and(skuEntity.name.lower().like("%" + skuCode.toLowerCase() + "%"))) // SKU 이름으로 검색
                        .exists()
      );
    }

    // 검색 조건 적용
    query.where(whereCondition);

    // 페이징 적용
    query.offset(pageable.getOffset());
    query.limit(pageable.getPageSize());

    // 정렬 적용
    pageable.getSort().forEach(order -> {
      com.querydsl.core.types.Order direction = order.isAscending() ?
                                                com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
      if ("createdDate".equals(order.getProperty())) {
        query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, productEntity.createdDate));
      } else if ("name".equals(order.getProperty())) {
        query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, productEntity.name));
      }
      // 필요한 다른 정렬 필드 추가 가능
    });

    // 쿼리 실행
    List<ResponseProductEntity> content = query.fetch();

    // productId 리스트로 얻어오기 (현재 페이지의 상품 ID들)
    List<Long> productIdsOnPage = content.stream().map(ResponseProductEntity::getId).toList();

    // 썸네일 이미지가 있는 경우에만 조회 (ProductColorVariantEntity를 통해 접근)
    if (!productIdsOnPage.isEmpty()) {
      // 상품 ID와 썸네일 이미지 URL을 Map으로 변환
      List<Tuple> productIdToThumbnailList = queryFactory
          .select(productEntity.id, imageUrlEntity.imageUrl) // ⭐ Product ID와 Image URL 선택
          .from(productEntity)
          .leftJoin(productEntity.colorVariants, productColorVariantEntity) // ⭐ Product -> ColorVariant 조인
          .leftJoin(productColorVariantEntity.images, imageUrlEntity) // ⭐ ColorVariant -> ImageUrl 조인
          .where(productEntity.id.in(productIdsOnPage) // 현재 페이지의 상품 ID만 필터링
                                 .and(imageUrlEntity.isThumbnail.eq(true))) // 썸네일 이미지인 경우만 선택
          .fetch();

      // Tuple 리스트를 Map으로 변환
      Map<Long, String> productIdToThumbnailMap = productIdToThumbnailList.stream()
                                                                          .collect(Collectors.toMap(
                                                                              tuple -> tuple.get(productEntity.id), // ⭐ Product ID를 키로 사용
                                                                              tuple -> tuple.get(imageUrlEntity.imageUrl), // ⭐ Image URL을 값으로 사용
                                                                              (existing, replacement) -> existing // 중복된 키가 있을 경우 기존 값 유지 (여러 썸네일이 있을 경우 첫 번째)
                                                                          ));

      // 각 상품에 썸네일 URL 설정
      content.forEach(product -> {
        String thumbnailUrl = productIdToThumbnailMap.get(product.getId());
        product.setThumbnailUrl(thumbnailUrl != null ? thumbnailUrl : ""); // null인 경우 빈 문자열로 설정
      });
    }

    // 총 카운트 쿼리 (페이징을 위해 필요)
    // countDistinct()는 group by 쿼리의 결과 수를 정확히 세기 위해 필요
    JPAQuery<Long> countQuery = queryFactory
        .select(productEntity.countDistinct()) // productEntity의 고유 개수만 세기
        .from(productEntity)
        .leftJoin(productEntity.category, categoryEntity)
        .leftJoin(productEntity.skus, skuEntity) // SKU 조인 유지 (skuCode 검색 조건 및 전체 product count에 영향 줄 수 있음)
        .where(whereCondition);

    // PageableExecutionUtils를 사용하여 Page 객체 반환
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
}