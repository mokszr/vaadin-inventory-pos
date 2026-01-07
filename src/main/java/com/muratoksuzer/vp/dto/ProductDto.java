package com.muratoksuzer.vp.dto;

import com.muratoksuzer.vp.entity.domain.ProductUnit;

public class ProductDto {

    private Long id;
    private String name;
    private String barcode;
    private ProductUnit unit = ProductUnit.PCS;
    private boolean active = true;

    public ProductDto() {
    }

    public ProductDto(String name, String barcode) {
        this.name = name;
        this.barcode = barcode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
