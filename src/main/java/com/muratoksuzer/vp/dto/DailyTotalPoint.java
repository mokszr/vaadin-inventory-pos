package com.muratoksuzer.vp.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public record DailyTotalPoint(Date day, BigDecimal total) {
    public LocalDate dayAsLocalDate() {
        return day.toLocalDate();
    }
}
