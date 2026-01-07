package com.muratoksuzer.vp.dto;

import java.math.BigDecimal;

public record LowStockRow(
        String productName,
        BigDecimal onHand,
        BigDecimal minRequired,
        BigDecimal missing
) {}
