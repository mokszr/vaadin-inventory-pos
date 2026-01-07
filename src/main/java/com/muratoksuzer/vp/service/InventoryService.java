package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.dto.StockItemDto;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.entity.domain.StockItem;
import com.muratoksuzer.vp.entity.domain.StockMovement;
import com.muratoksuzer.vp.entity.domain.StockMovementType;
import com.muratoksuzer.vp.exception.AppLevelValidationException;
import com.muratoksuzer.vp.repository.ProductRepository;
import com.muratoksuzer.vp.repository.StockItemRepository;
import com.muratoksuzer.vp.repository.StockMovementRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
@Service
public class InventoryService {

    private final StockItemRepository stockRepo;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final TranslationService translations;

    public InventoryService(StockItemRepository stockRepo, StockMovementRepository stockMovementRepository, ProductRepository productRepository, TranslationService translations) {
        this.stockRepo = stockRepo;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.translations = translations;

    }

    public StockItem ensureStock(Product product) {
        return stockRepo.findByProductId(product.getId()).orElseGet(() -> stockRepo.save(new StockItem(product)));
    }

    public void increaseStock(Long productId, BigDecimal quantity, String note) {
        Optional<StockItem> byProductId = Optional.ofNullable(stockRepo.findByProductId(productId).orElseThrow(() -> new IllegalArgumentException("no stock item found")));
        StockItem s = byProductId.get();
        s.setQuantityOnHand(s.getQuantityOnHand().add(quantity));
        stockRepo.save(s);

        StockMovement stockMovement = new StockMovement(productRepository.findById(productId).get(), StockMovementType.IN, quantity, note);
        stockMovementRepository.save(stockMovement);
    }

    public void decreaseStock(Long productId, BigDecimal quantity, String note) {
        Optional<StockItem> byProductId = Optional.ofNullable(stockRepo.findByProductId(productId).orElseThrow(() -> new IllegalArgumentException("no stock item found")));
        StockItem s = byProductId.get();

        if(s.getQuantityOnHand().compareTo(quantity) < 0 ) {
            throw new AppLevelValidationException(translations.t("inventory.insufficientStock"));
        }

        s.setQuantityOnHand(s.getQuantityOnHand().subtract(quantity));
        stockRepo.save(s);

        StockMovement stockMovement = new StockMovement(productRepository.findById(productId).get(), StockMovementType.OUT, quantity, note);
        stockMovementRepository.save(stockMovement);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<StockItemDto> findPaginated(Pageable pageable, String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(stockRepo.findAll(pageable));
        }

        return mapToDto(stockRepo.findByProductNameContainingIgnoreCaseOrProductBarcodeContaining(searchTerm, searchTerm, pageable));

    }

    public void updateReorderLevel(Long productId, BigDecimal reorderLevel, String location) {
        StockItem si = stockRepo.findByProductId(productId)
                .orElseThrow(() -> new AppLevelValidationException(
                        translations.t("pos.stock.missing", productId)
                ));

        si.setReorderLevel(reorderLevel == null ? BigDecimal.ZERO : reorderLevel);
        si.setLocation(location);
        stockRepo.save(si);
    }

    private Page<StockItemDto> mapToDto(Page<StockItem> page) {
        return page.map(stockItem -> toDto(stockItem));
    }

    private StockItemDto toDto(StockItem stockItem) {
        if (stockItem == null) {
            return null;
        }
        StockItemDto dto = new StockItemDto();
        dto.setId(stockItem.getId());
        dto.setLocation(stockItem.getLocation());
        dto.setReorderLevel(stockItem.getReorderLevel());
        dto.setQuantityOnHand(stockItem.getQuantityOnHand());
        dto.setProduct(toDto(stockItem.getProduct()));

        return dto;
    }

    private ProductDto toDto(Product product) {
        if (product == null) {
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

}
