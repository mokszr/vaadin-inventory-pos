package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.PriceDto;
import com.muratoksuzer.vp.dto.UiCartLineDto;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.service.PosService;
import com.muratoksuzer.vp.service.PriceService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Route(value = "pos", layout = MainLayout.class)
@PageTitle("POS")
@RolesAllowed({"USER", "ADMIN"})
public class PosView extends VerticalLayout {

    // key = productId
    private final Map<Long, UiCartLineDto> cart = new LinkedHashMap<>();

    private final PosService posService;
    private final Grid<UiCartLineDto> grid = new Grid<>(UiCartLineDto.class, false);
    private final TextField barcodeField;
    private final PriceService priceService;
    private TranslationService translations;

    public PosView(PosService posService, PriceService priceService, TranslationService translations) {
        this.posService = posService;
        this.priceService = priceService;
        this.translations = translations;
        setSizeFull();

        HorizontalLayout barcodeLayout = new HorizontalLayout();

        Button searchButton = new Button(translations.t("general.search.button"),
                new Icon(VaadinIcon.SEARCH));
        searchButton.setIconAfterText(true);
        searchButton.addClickListener(buttonClickEvent -> onBarcodeEntered());

        barcodeField = new TextField(translations.t("pos.scanEnterBarcode"));
        barcodeField.setPlaceholder(translations.t("pos.scanEnterBarcodePlaceholder"));
        barcodeField.setClearButtonVisible(true);
        barcodeField.setWidthFull();

        barcodeField.addKeyPressListener(Key.ENTER, e -> onBarcodeEntered());

        barcodeLayout.add(barcodeField);
        barcodeLayout.add(searchButton);

        barcodeLayout.setFlexGrow(1, barcodeField);

        barcodeLayout.setWidthFull();
        barcodeLayout.setJustifyContentMode(JustifyContentMode.END);
        barcodeLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        grid.addColumn(l -> l.getProduct().getName()).setHeader(translations.t("pos.grid.product")).setFlexGrow(1);
        grid.addColumn(UiCartLineDto::getQuantity).setHeader(translations.t("pos.grid.qty")).setAutoWidth(true);
        grid.addColumn(UiCartLineDto::getUnitPrice).setHeader(translations.t("pos.grid.unitPrice")).setAutoWidth(true);
        grid.addColumn(UiCartLineDto::getLineTotal).setHeader(translations.t("pos.grid.lineTotal")).setAutoWidth(true);

        grid.addComponentColumn(line -> {
            Button minus = new Button("-", e -> { line.setQuantity(line.getQuantity().subtract(new BigDecimal("1"))); normalize(line); });
            Button plus = new Button("+", e -> { line.setQuantity(line.getQuantity().add(new BigDecimal("1"))); normalize(line); });
            Button remove = new Button(translations.t("button.remove"), e -> { cart.remove(line.getProduct().getId()); refreshGrid(); });
            return new HorizontalLayout(minus, plus, remove);
        }).setHeader(translations.t("general.columns.actions")).setAutoWidth(true);

        Button checkout = new Button(translations.t("pos.checkout"), e -> {
            try {
                var lines = cart.values().stream()
                        .map(l -> new PosService.CartLine(l.getProduct(), l.getQuantity(), l.getUnitPrice()))
                        .toList();

                var sale = posService.checkout(lines);
                NotificationSupport.showInfo(translations.t(
                        "pos.checkout.success",
                        sale.getSaleNo(),
                        sale.getTotal()
                ));
                cart.clear();
                refreshGrid();
                barcodeField.focus();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout actions = new HorizontalLayout(checkout);
        actions.setDefaultVerticalComponentAlignment(Alignment.END);

        add(barcodeLayout, actions, grid);
        barcodeField.focus();
        refreshGrid();
    }

    private void onBarcodeEntered() {
        String barcode = barcodeField.getValue() == null ? "" : barcodeField.getValue().trim();
        barcodeField.clear();
        if (barcode.isBlank()) {
            return;
        }

        Product p = posService.findProductByBarcode(barcode);
        if (p == null) {
            NotificationSupport.showError(translations.t("pos.productNotFound", barcode));
            return;
        }

        PriceDto activePrice = priceService.getActivePrice(p.getId());
        if(activePrice == null) {
            NotificationSupport.showError(translations.t("pos.activePriceNotFound", p.getName()));
            return;
        }

        UiCartLineDto line = cart.get(p.getId());
        if (line == null) {
            cart.put(p.getId(), new UiCartLineDto(p, new BigDecimal("1"), activePrice.getAmount()));
        } else {
            line.setQuantity(line.getQuantity().add(new BigDecimal("1")));
        }
        refreshGrid();
    }

    private void normalize(UiCartLineDto line) {
        if (line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            cart.remove(line.getProduct().getId());
        }
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(cart.values());
    }

}
