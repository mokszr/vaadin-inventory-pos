package com.muratoksuzer.vp.entity.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "sale",
        indexes = @Index(name = "ix_sale_date_created", columnList = "dateCreated"),
        uniqueConstraints = @UniqueConstraint(name = "uq_sale_sale_no", columnNames = "saleNo"))
public class Sale extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String saleNo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    public Sale() {
    }

    public Sale(String saleNo) {
        this.saleNo = saleNo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getSaleNo() {
        return saleNo;
    }

    public void setSaleNo(String saleNo) {
        this.saleNo = saleNo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
