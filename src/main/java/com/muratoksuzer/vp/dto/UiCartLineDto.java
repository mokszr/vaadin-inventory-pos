package com.muratoksuzer.vp.dto;

import com.muratoksuzer.vp.entity.domain.Product;

import java.math.BigDecimal;

public class UiCartLineDto {
    private final Product product;
    private BigDecimal quantity;
    private BigDecimal unitPrice;

    public UiCartLineDto(Product product, BigDecimal quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(quantity);
    }
}