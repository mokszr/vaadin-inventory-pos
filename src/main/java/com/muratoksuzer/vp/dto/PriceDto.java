package com.muratoksuzer.vp.dto;

import com.muratoksuzer.vp.entity.domain.Currency;
import java.math.BigDecimal;

public class PriceDto {
    private Long id;
    private Long productId;
    private BigDecimal amount;
    private Currency currency;
    private boolean active = true;

    public PriceDto() {
    }

    public PriceDto(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
