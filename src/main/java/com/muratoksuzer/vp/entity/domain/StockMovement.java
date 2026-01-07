package com.muratoksuzer.vp.entity.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_movement",
        indexes = {
                @Index(name = "ix_movement_date_created", columnList = "dateCreated"),
                @Index(name = "ix_movement_product", columnList = "product_id")
        })
public class StockMovement extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StockMovementType type;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity; // positive number

    @Column(length = 512)
    private String note;

    public StockMovement() {
    }

    public StockMovement(Product product, StockMovementType type, BigDecimal quantity, String note) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StockMovementType getType() {
        return type;
    }

    public void setType(StockMovementType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
