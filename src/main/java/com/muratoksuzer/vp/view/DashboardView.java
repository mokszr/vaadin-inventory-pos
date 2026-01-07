package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.DashboardKpis;
import com.muratoksuzer.vp.dto.LowStockRow;
import com.muratoksuzer.vp.dto.SalesSeries;
import com.muratoksuzer.vp.dto.TopProductsSeries;
import com.muratoksuzer.vp.service.DashboardServiceImpl;
import com.muratoksuzer.vp.view.subview.ChartJs;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Route(value= "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
@RolesAllowed({"USER", "ADMIN"})
public class DashboardView extends VerticalLayout {

    private final DashboardServiceImpl dashboardService;

    private final Span revenueToday = new Span("-");
    private final Span revenueThisMonth = new Span("-");
    private final Span salesToday = new Span("-");
    private final Span lowStockCount = new Span("-");

    private final ChartJs salesLineChart = new ChartJs(ChartJs.Type.LINE);
    private final ChartJs topProductsBarChart = new ChartJs(ChartJs.Type.BAR);

    private final Grid<LowStockRow> lowStockGrid = new Grid<>(LowStockRow.class, false);

    public DashboardView(DashboardServiceImpl dashboardService) {
        this.dashboardService = dashboardService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(header(), kpiRow(), chartsRow(), lowStockSection());

        refresh();
    }

    private Component header() {
        H2 title = new H2("Dashboard");
        title.getStyle().setMargin("0");
        return title;
    }

    private Component kpiRow() {
        HorizontalLayout row = new HorizontalLayout(
                kpiCard("Revenue (Today)", revenueToday),
                kpiCard("Revenue (This Month)", revenueThisMonth),
                kpiCard("Sales (Today)", salesToday),
                kpiCard("Low Stock Products", lowStockCount)
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.setFlexGrow(1, row.getComponentAt(0), row.getComponentAt(1), row.getComponentAt(2), row.getComponentAt(3));
        return row;
    }

    private Component kpiCard(String label, Span value) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidthFull();
        card.getStyle()
                .setBorder("1px solid var(--lumo-contrast-10pct)")
                .setBorderRadius("12px")
                .setBoxShadow("var(--lumo-box-shadow-xs)");

        Span lbl = new Span(label);
        lbl.getStyle().setColor("var(--lumo-secondary-text-color)");
        lbl.getStyle().setFontSize("var(--lumo-font-size-s)");

        value.getStyle().setFontSize("var(--lumo-font-size-xxl)");
        value.getStyle().setFontWeight("700");

        card.add(lbl, value);
        return card;
    }

    private Component chartsRow() {
        salesLineChart.setWidthFull();
        salesLineChart.setHeight(320, Unit.PIXELS);

        topProductsBarChart.setWidthFull();
        topProductsBarChart.setHeight(320, Unit.PIXELS);

        VerticalLayout left = new VerticalLayout(new Span("Sales (Last 14 days)"), salesLineChart);
        left.setPadding(false);
        left.setSpacing(true);
        left.setWidthFull();

        VerticalLayout right = new VerticalLayout(new Span("Top Products (Revenue)"), topProductsBarChart);
        right.setPadding(false);
        right.setSpacing(true);
        right.setWidthFull();

        VerticalLayout row = new VerticalLayout(left, right);
        row.setWidthFull();
        row.setSpacing(true);
        row.setFlexGrow(1, left, right);

        return row;
    }

    private Component lowStockSection() {
        lowStockGrid.addColumn(LowStockRow::productName).setHeader("Product").setFlexGrow(1);
        lowStockGrid.addColumn(LowStockRow::onHand).setHeader("On Hand").setAutoWidth(true);
        lowStockGrid.addColumn(LowStockRow::minRequired).setHeader("Min").setAutoWidth(true);
        lowStockGrid.addColumn(LowStockRow::missing).setHeader("Missing").setAutoWidth(true);

        lowStockGrid.setWidthFull();
        lowStockGrid.setHeight("320px");

        VerticalLayout wrap = new VerticalLayout(new Span("Low Stock / Insufficient Quantity on Hand"), lowStockGrid);
        wrap.setPadding(false);
        wrap.setSpacing(true);
        wrap.setWidthFull();
        return wrap;
    }

    private void refresh() {
        LocalDate today = LocalDate.now();

        DashboardKpis kpis = dashboardService.getKpis(today);
        revenueToday.setText(formatMoney(kpis.revenueToday()));
        revenueThisMonth.setText(formatMoney(kpis.revenueThisMonth()));
        salesToday.setText(String.valueOf(kpis.salesCountToday()));
        lowStockCount.setText(String.valueOf(kpis.lowStockProductCount()));

        SalesSeries salesSeries = dashboardService.getDailySalesSeries(today.minusDays(13), today);
        salesLineChart.setSeries(
                salesSeries.labels(),
                "Revenue",
                salesSeries.values()
        );

        TopProductsSeries top = dashboardService.getTopProductsByRevenue(today.withDayOfMonth(1), today, 10);
        topProductsBarChart.setSeries(
                top.labels(),
                "Revenue",
                top.values()
        );

        lowStockGrid.setItems(dashboardService.getLowStockRows(25));
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) return "-";
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US); // change if needed
        return nf.format(v);
    }
}
