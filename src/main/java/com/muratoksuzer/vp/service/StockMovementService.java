package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.dto.StockMovementDto;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.entity.domain.StockMovement;
import com.muratoksuzer.vp.repository.StockMovementRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service
public class StockMovementService {

    private StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<StockMovementDto> findPaginated(Pageable pageable, String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(stockMovementRepository.findAll(pageable));
        }

        return mapToDto(stockMovementRepository.findByProductNameContainingIgnoreCaseOrProductBarcodeContaining(searchTerm, searchTerm, pageable));

    }

    private Page<StockMovementDto> mapToDto(Page<StockMovement> page) {
        return page.map(stockItem -> toDto(stockItem));
    }

    private StockMovementDto toDto(StockMovement stockMovement) {
        if (stockMovement == null) {
            return null;
        }
        StockMovementDto dto = new StockMovementDto();
        dto.setId(stockMovement.getId());
        dto.setNote(stockMovement.getNote());
        dto.setQuantity(stockMovement.getQuantity());
        dto.setType(stockMovement.getType());
        dto.setProduct(toDto(stockMovement.getProduct()));
        dto.setDateCreated(stockMovement.getDateCreated());
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
