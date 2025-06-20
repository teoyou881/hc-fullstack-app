package teo.springjwt.product.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teo.springjwt.category.CategoryEntity;
import teo.springjwt.category.repository.CategoryRepository;
import teo.springjwt.product.dto.ResponseProductEntity;
import teo.springjwt.product.dto.ResponseSkuDTO;
import teo.springjwt.product.dto.request.RequestProductCreate;
import teo.springjwt.product.dto.request.RequestProductCreate.ProductOptionGroupRequest;
import teo.springjwt.product.dto.request.RequestProductCreate.ProductOptionValueRequest;
import teo.springjwt.product.entity.OptionGroupEntity;
import teo.springjwt.product.entity.OptionValueEntity;
import teo.springjwt.product.entity.ProductColorVariantEntity;
import teo.springjwt.product.entity.ProductEntity;
import teo.springjwt.product.entity.ProductOptionGroupEntity;
import teo.springjwt.product.entity.ProductOptionValueEntity;
import teo.springjwt.product.entity.SkuEntity;
import teo.springjwt.product.entity.SkuOptionValueEntity;
import teo.springjwt.product.repository.group.OptionGroupEntityRepository;
import teo.springjwt.product.repository.product.ProductColorVariantEntityRepository;
import teo.springjwt.product.repository.product.ProductEntityRepository;
import teo.springjwt.product.repository.sku.SkuOptionValueRepository;
import teo.springjwt.product.repository.sku.SkuRepository;
import teo.springjwt.product.repository.value.OptionValueEntityRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

  private final ProductEntityRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final OptionGroupEntityRepository groupRepository;
  private final OptionValueEntityRepository optionRepository;
  private final SkuRepository skuRepository;
  private final SkuOptionValueRepository skuOptionValueRepository;
  private final ProductColorVariantEntityRepository colorVariantRepository;

  public ResponseEntity<List<ResponseProductEntity>> getAllProducts() {
    List<ProductEntity> all = productRepository.findAll();

    BigDecimal minPrice = all.stream().flatMap(product -> product.getSkus() != null ? product.getSkus().stream() : null)
                             .map(SkuEntity::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);


    List<ResponseProductEntity> list = productRepository.findAll().stream().map(ResponseProductEntity::from).toList();
    // list.forEach(product -> product.setMinPrice(minPrice));
    return ResponseEntity.ok(list);
  }

  public Page<ResponseProductEntity> getAllProductsWithMinPriceAndMaxPrice(String name, String skuCode,
      Pageable pageable) {
    return productRepository.findAllProductsWithMinPriceAndMaxPrice(name, skuCode, pageable);
  }


  public ProductEntity createProduct(RequestProductCreate request) {

    // 1. CategoryEntity 조회
    CategoryEntity category = categoryRepository
        .findById(request.getCategoryId())
        .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + request.getCategoryId()));
    // 2. ProductEntity 생성 및 저장
    ProductEntity product = new ProductEntity(request.getName(), request.getDescription(), category);
    productRepository.save(product); // 먼저 저장하여 ID를 할당받음

    // colorVariantEntity 생성 및 저장
    // ⭐ toList() 대신 Collectors.toCollection(ArrayList::new) 사용
    List<ProductColorVariantEntity> colorVariants = request
        .getColors()
        .stream()
        .map(color -> new ProductColorVariantEntity(product, color.trim()))
        .collect(Collectors.toCollection(ArrayList::new)); // ⭐ 이 부분 변경

    colorVariantRepository.saveAll(colorVariants);
    product.setColorVariants(colorVariants); // ProductEntity의 colorVariants 리스트도 업데이트 (양방향 관계)
    productRepository.save(product); // Product 업데이트 (colorVariants 반영)

    // 3. ProductOptionGroupEntity 및 ProductOptionValueEntity 처리
    List<ProductOptionGroupEntity> savedOptionGroups = new ArrayList<>();
    if (request.getOptionGroups() != null) {
      for (ProductOptionGroupRequest groupRequest : request.getOptionGroups()) {
        OptionGroupEntity groupEntity = groupRepository.getOptionGroupEntityById(groupRequest.getId());
        ProductOptionGroupEntity optionGroup = new ProductOptionGroupEntity(
            product,
            groupEntity,
            groupEntity.getDisplayOrder());

        if (groupRequest.getOptionValues() != null) {
          for (ProductOptionValueRequest valueRequest : groupRequest.getOptionValues()) {
            OptionValueEntity ov = optionRepository
                .findById(valueRequest.getId())
                .orElseThrow(() -> new EntityNotFoundException("Option not found with ID: " + valueRequest.getId()));

            ProductOptionValueEntity optionValue = new ProductOptionValueEntity(
                optionGroup,
                ov,
                ov.getDisplayOrder(),
                ov.getExtraPrice());

            optionGroup.addOptionValue(optionValue);
          }
        }
        product.addProductOptionGroup(optionGroup);
        savedOptionGroups.add(optionGroup);
      }
    }
    productRepository.flush();
    // 4. SKU 조합 생성 및 저장
    generateAndSaveSkus(product, request.getPrice(), savedOptionGroups, new ArrayList<>(), 0, colorVariants);

    return product;
  }

  /**
   * 상품의 모든 옵션 조합에 대한 SKU를 생성하고 저장하는 재귀 메서드.
   *
   * @param product             생성 중인 상품 엔티티
   * @param basePrice           상품의 기본 가격
   * @param productOptionGroups 상품에 연결된 ProductOptionGroupEntity 리스트
   * @param currentCombination  현재까지 선택된 ProductOptionValueEntity 조합 (재귀 호출 시 누적)
   * @param groupIndex          현재 처리 중인 ProductOptionGroup의 인덱스
   * @param colorVariants       상품이 가지고 있는 색상 리스트
   */
  private void generateAndSaveSkus(ProductEntity product, BigDecimal basePrice,
      List<ProductOptionGroupEntity> productOptionGroups, List<ProductOptionValueEntity> currentCombination,
      int groupIndex, List<ProductColorVariantEntity> colorVariants) {
    // 모든 옵션 그룹에 대한 조합이 완료되면 SKU 생성
    if (groupIndex == productOptionGroups.size()) {
      for (ProductColorVariantEntity colorVariant : colorVariants) {
        BigDecimal finalPrice = basePrice;
        StringBuilder skuNameBuilder = new StringBuilder(product.getName()); // SKU 이름의 기본값은 상품명
        StringBuilder skuDescriptionBuilder = new StringBuilder(); // SKU 설명 (옵션 값들의 조합으로 만듦)

        // SKU에 포함될 ProductOptionValueEntity 리스트 (SKU와 OptionValue 간의 다대다 관계를 위해 필요)
        List<ProductOptionValueEntity> skuRelatedOptionValues = new ArrayList<>();

        for (ProductOptionValueEntity value : currentCombination) {
          finalPrice = finalPrice.add(value.getExtraPrice());
          skuNameBuilder.append(" - ").append(value.getOptionValue().getName()); // 마스터 옵션 값 이름 사용

          if (!skuDescriptionBuilder.isEmpty()) {
            skuDescriptionBuilder.append(", ");
          }
          // '그룹명: 값명' 형태로 설명 생성
          skuDescriptionBuilder.append(value.getProductOptionGroup().getOptionGroup().getName()).append(": ").append(
              value.getOptionValue().getName());

          skuRelatedOptionValues.add(value);
        }

        // ⭐ 색상 정보 추가
        skuNameBuilder.append(" - ").append(colorVariant.getColorName());
        if (!skuDescriptionBuilder.isEmpty()) {
          skuDescriptionBuilder.append(", ");
        }
        skuDescriptionBuilder.append(colorVariant.getColorName());

        // ⭐ SKU 엔티티 생성
        SkuEntity sku = SkuEntity.builder()
                                 .product(product)
                                 .colorVariant(colorVariant) // ⭐ ProductColorVariantEntity 연결
                                 .price(finalPrice)
                                 .stock(100) // 기본 재고 100으로 설정
                                 .name(skuNameBuilder.toString().trim()) // skuCode 필드 사용
                                 .description(skuDescriptionBuilder
                                                  .toString()
                                                  .trim()).build();
        skuRepository.save(sku); // SKU 저장

        // ⭐ SKU와 ProductOptionValue 간의 관계 (SkuOptionValueEntity) 저장
        for (ProductOptionValueEntity productOptionValue : skuRelatedOptionValues) {
          SkuOptionValueEntity skuOptionValue = SkuOptionValueEntity.builder().sku(sku).productOptionValue(
              productOptionValue).build();
          skuOptionValueRepository.save(skuOptionValue);
          sku.addSkuOptionValue(skuOptionValue); // 양방향 관계 설정 (SkuEntity에도 addSkuOptionValue 필요)
        }

        // ProductEntity에도 SKU 추가 (양방향 관계 설정)
        product.addSku(sku);

      }
      return; // 재귀 종료 (모든 색상 변형에 대해 SKU 생성 완료)
    }

    // 현재 옵션 그룹의 모든 옵션 값들을 순회하며 재귀 호출
    ProductOptionGroupEntity currentGroup = productOptionGroups.get(groupIndex);
    for (ProductOptionValueEntity productOptionValue : currentGroup.getProductOptionValues()) { // getProductOptionValues() 사용
      currentCombination.add(productOptionValue); // 현재 조합에 추가
      generateAndSaveSkus(
          product,
          basePrice,
          productOptionGroups,
          currentCombination,
          groupIndex + 1,
          colorVariants); // 다음 그룹으로 재귀 호출
      currentCombination.removeLast(); // 백트래킹: 다음 조합을 위해 마지막 추가된 값 제거 (removeLast() 대신 인덱스로 제거)
    }
  }

  public ResponseEntity<List<ResponseSkuDTO>> getProductById(Long productId) {
    return ResponseEntity.ok(skuRepository
                                 .findBySkuWithProductId(productId)
                                 .stream()
                                 .map(ResponseSkuDTO::fromEntity)
                                 .toList());
  }
}