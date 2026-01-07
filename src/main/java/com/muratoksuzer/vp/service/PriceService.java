package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.PriceDto;
import com.muratoksuzer.vp.entity.domain.Price;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.exception.AppLevelValidationException;
import com.muratoksuzer.vp.repository.PriceRepository;
import com.muratoksuzer.vp.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
@Service
public class PriceService {

    private PriceRepository priceRepository;
    private ProductRepository productRepository;

    public PriceService(PriceRepository priceRepository, ProductRepository productRepository) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<PriceDto> findPaginated(Long productId, Pageable pageable) {

        return mapToDto(priceRepository.findByProductIdOrderByActiveDesc(productId, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public PriceDto getActivePrice(Long productId) {
        Price activePrice = priceRepository.findByProductIdAndActive(productId, true);
        return toDto(activePrice);
    }

    private Page<PriceDto> mapToDto(Page<Price> page) {
        return page.map(price -> toDto(price));
    }

    private PriceDto toDto(Price price) {
        if (price == null) {
            return null;
        }
        PriceDto dto = new PriceDto();
        dto.setId(price.getId());
        dto.setActive(price.isActive());
        dto.setAmount(price.getAmount());
        dto.setCurrency(price.getCurrency());
        return dto;
    }


    public void createPriceAndSwitchActiveIfRequested(PriceDto dto, Long productId) {
        Price entity = mapToEntity(dto);
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppLevelValidationException("no product found"));
        entity.setProduct(product);

        // if saved one is active, disable current active one
        if (dto.isActive()) {
            Price activePrice = priceRepository.findByProductIdAndActive(productId, true);
            if (activePrice != null) {
                activePrice.setActive(false);
                priceRepository.save(activePrice);
            }
        }

        priceRepository.save(entity);
    }

    public void activatePrice(Long priceId, Long productId) {
        Optional<Price> byId = priceRepository.findById(priceId);
        if(byId.isPresent()) {
            Price price = byId.get();
            Price activePrice = priceRepository.findByProductIdAndActive(productId, true);
            if (activePrice != null) {
                activePrice.setActive(false);
                priceRepository.save(activePrice);
                priceRepository.flush();
            }
            price.setActive(true);
            priceRepository.save(price);
        }

    }

    public void delete(Long priceId) {
        priceRepository.deleteById(priceId);
    }

    private Price mapToEntity(PriceDto dto) {
        Price price = new Price();
        price.setActive(dto.isActive());
        price.setId(dto.getId());
        price.setAmount(dto.getAmount());
        price.setCurrency(dto.getCurrency());

        return price;
    }
}
