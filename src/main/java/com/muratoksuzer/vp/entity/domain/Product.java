package com.muratoksuzer.vp.entity.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "product",
        indexes = {
                @Index(name = "ix_product_barcode", columnList = "barcode")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_product_barcode", columnNames = "barcode")
        })
public class Product extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 64)
    private String barcode;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ProductUnit unit = ProductUnit.PCS;

    @Column(nullable = false)
    private boolean active = true;

    public Product() {
    }

    public Product(String name, String barcode) {
        this.name = name;
        this.barcode = barcode;
    }

    public Long getId() {
        return id;
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

    @Override
    public String toString() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

