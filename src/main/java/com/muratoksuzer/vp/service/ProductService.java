package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.entity.domain.StockItem;
import com.muratoksuzer.vp.exception.AppLevelValidationException;
import com.muratoksuzer.vp.repository.ProductRepository;
import com.muratoksuzer.vp.repository.StockItemRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final StockItemRepository stockItemRepo;
    private TranslationService translationService;


    public ProductService(ProductRepository productRepo, StockItemRepository stockItemRepo, TranslationService translationService) {
        this.productRepo = productRepo;
        this.stockItemRepo = stockItemRepo;
        this.translationService = translationService;
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    @Transactional
    public Product saveProduct(ProductDto productDto) {

        Product alreadySavedOne = productRepo.findByNameIgnoreCase(productDto.getName());
        // new product creation
        if (productDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // product update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(productDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        Product entity = mapToEntity(productDto);
        Product saved = productRepo.save(entity);

        // Ensure StockItem exists (nice for demo)
        stockItemRepo.findByProductId(saved.getId())
                .orElseGet(() -> stockItemRepo.save(new StockItem(saved)));

        return saved;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<ProductDto> findPaginated(Pageable pageable, String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(productRepo.findAll(pageable));
        }

        return mapToDto(productRepo.findByNameContainingIgnoreCase(searchTerm, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ProductDto getProduct(Long productId) {
        Optional<Product> byId = productRepo.findById(productId);
        return byId.map(this::toDto).orElse(null);
    }

    private Page<ProductDto> mapToDto(Page<Product> productPage) {
        return productPage.map(product -> toDto(product));
    }

    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        product.setActive(true);
        product.setBarcode(dto.getBarcode());
        product.setName(dto.getName());
        product.setUnit(dto.getUnit());
        product.setId(dto.getId());
        return product;
    }

    private ProductDto toDto(Product product) {
        if(product == null) {
            return null;
        }
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setActive(product.isActive());
        dto.setBarcode(product.getBarcode());
        dto.setUnit(product.getUnit());
        return dto;
    }

    public void delete(ProductDto p) {
        productRepo.deleteById(p.getId());
    }


}
