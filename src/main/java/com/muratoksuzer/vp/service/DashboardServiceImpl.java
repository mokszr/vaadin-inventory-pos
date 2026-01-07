package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.dto.DashboardKpis;
import com.muratoksuzer.vp.dto.LowStockRow;
import com.muratoksuzer.vp.dto.SalesSeries;
import com.muratoksuzer.vp.dto.TopProductPoint;
import com.muratoksuzer.vp.dto.TopProductsSeries;
import com.muratoksuzer.vp.repository.SaleLineRepository;
import com.muratoksuzer.vp.repository.SaleRepository;
import com.muratoksuzer.vp.repository.StockItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl {

    private static final ZoneId UI_ZONE = ZoneId.of("Europe/Istanbul");
    private static final DateTimeFormatter LABEL_FMT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final SaleRepository saleRepo;
    private final SaleLineRepository saleLineRepo;
    private final StockItemRepository stockItemRepo;

    public DashboardServiceImpl(SaleRepository saleRepo,
                                SaleLineRepository saleLineRepo,
                                StockItemRepository stockItemRepo) {
        this.saleRepo = saleRepo;
        this.saleLineRepo = saleLineRepo;
        this.stockItemRepo = stockItemRepo;
    }


    public DashboardKpis getKpis(LocalDate today) {
        OffsetDateTime startToday = startOfDay(today);
        OffsetDateTime startTomorrow = startOfDay(today.plusDays(1));

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        OffsetDateTime startMonth = startOfDay(firstDayOfMonth);

        BigDecimal revenueToday = saleRepo.sumTotalBetween(startToday, startTomorrow);
        BigDecimal revenueThisMonth = saleRepo.sumTotalBetween(startMonth, startTomorrow);
        long salesCountToday = saleRepo.countBetween(startToday, startTomorrow);
        long lowStockCount = stockItemRepo.countLowStock();

        return new DashboardKpis(revenueToday, revenueThisMonth, salesCountToday, lowStockCount);
    }


    public SalesSeries getDailySalesSeries(LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = startOfDay(from);
        OffsetDateTime toExclusive = startOfDay(to.plusDays(1));

        List<Object[]> rows = saleRepo.dailyTotalsRaw(fromDt, toExclusive);
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            Object dayObj = r[0];
            Object totalObj = r[1];

            LocalDate day;
            if (dayObj instanceof java.sql.Date d) {
                day = d.toLocalDate();
            } else if (dayObj instanceof LocalDate ld) {
                day = ld;
            } else if (dayObj instanceof java.time.OffsetDateTime odt) {
                day = odt.toLocalDate();
            } else {
                throw new IllegalStateException("Unsupported day type: " + (dayObj == null ? "null" : dayObj.getClass()));
            }

            BigDecimal total = (totalObj instanceof BigDecimal bd) ? bd : BigDecimal.ZERO;
            map.put(day, total);
        }


        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        LocalDate d = from;
        while (!d.isAfter(to)) {
            labels.add(d.format(LABEL_FMT));
            values.add(map.getOrDefault(d, BigDecimal.ZERO));
            d = d.plusDays(1);
        }

        return new SalesSeries(labels, values);
    }


    public TopProductsSeries getTopProductsByRevenue(LocalDate from, LocalDate to, int limit) {
        OffsetDateTime fromDt = startOfDay(from);
        OffsetDateTime toExclusive = startOfDay(to.plusDays(1));

        List<TopProductPoint> rows = saleLineRepo.topProductsByRevenue(
                fromDt, toExclusive,
                PageRequest.of(0, Math.max(1, limit))
        );

        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        for (TopProductPoint r : rows) {
            labels.add(r.productName());
            values.add(r.total() == null ? BigDecimal.ZERO : r.total());
        }

        return new TopProductsSeries(labels, values);
    }


    public List<LowStockRow> getLowStockRows(int limit) {
        return stockItemRepo.findLowStock(PageRequest.of(0, Math.max(1, limit)));
    }

    private static OffsetDateTime startOfDay(LocalDate day) {
        // Create a ZonedDateTime in Istanbul, then convert to OffsetDateTime
        return day.atStartOfDay(UI_ZONE).toOffsetDateTime();
    }
}
