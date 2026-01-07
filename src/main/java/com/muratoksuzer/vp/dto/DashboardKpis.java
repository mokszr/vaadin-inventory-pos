package com.muratoksuzer.vp.dto;

import java.math.BigDecimal;

public record DashboardKpis(
        BigDecimal revenueToday,
        BigDecimal revenueThisMonth,
        long salesCountToday,
        long lowStockProductCount
) {}
