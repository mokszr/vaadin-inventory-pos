package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.StockItemDto;
import com.muratoksuzer.vp.service.InventoryService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.muratoksuzer.vp.view.components.PaginationView;
import com.muratoksuzer.vp.view.components.SearchBarView;
import com.muratoksuzer.vp.view.subview.DecreaseInventoryDialog;
import com.muratoksuzer.vp.view.subview.IncreaseInventoryDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

@Route(value = "inventory", layout = MainLayout.class)
@PageTitle("Inventory")
@RolesAllowed({"USER", "ADMIN"})
public class InventoryView extends VerticalLayout {

    private final InventoryService inventoryService;
    private final TranslationService translations;
    private PaginationView paginationView;
    private Grid<StockItemDto> stockItemGrid;

    public InventoryView(InventoryService inventoryService, TranslationService translations) {
        this.inventoryService = inventoryService;
        this.translations = translations;

        setSizeFull();
        add(new H2(translations.t("inventory.productInventory")));

        SearchBarView searchBarView = new SearchBarView(translations) {

            @Override
            public void searchClicked(String searchValue) {
                paginationView.setSearchTerm(searchValue);
                long foundCount = paginationView.loadPage(0);
                NotificationSupport.showInfo(translations.t("general.search.foundNotification", foundCount));
            }
        };

        searchBarView.setJustifyContentMode(JustifyContentMode.END);

        add(searchBarView);

        this.stockItemGrid = createGrid();

        add(stockItemGrid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<StockItemDto> page = inventoryService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                stockItemGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPageProduct");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());

    }

    private Grid<StockItemDto> createGrid() {
        Grid<StockItemDto> grid = new Grid<>(StockItemDto.class, false);
        grid.setSizeFull();
        grid.setEmptyStateText(translations.t("general.noItems"));

        grid.addColumn(si -> si.getProduct().getBarcode()).setHeader("Barcode").setAutoWidth(true);
        grid.addColumn(si -> si.getProduct().getName()).setHeader("Product").setFlexGrow(1);
        grid.addColumn(StockItemDto::getQuantityOnHand).setHeader("On Hand").setAutoWidth(true);
        grid.addColumn(StockItemDto::getReorderLevel).setHeader("Reorder").setAutoWidth(true);
        grid.addColumn(StockItemDto::getLocation).setHeader("Location").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(stockItemDto -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setPadding(false);

            Icon plusIcon = VaadinIcon.PLUS.create();
            plusIcon.setColor("grey");

            Button increaseButton = new Button(plusIcon);
            increaseButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            increaseButton.setTooltipText(translations.t("inventory.increase"));
            increaseButton.addClickListener(buttonClickEvent -> {
                new IncreaseInventoryDialog(inventoryService, translations, stockItemDto.getProduct(), v -> paginationView.loadCurrentPageAfterAddition()).open();
            });

            Icon minusIcon = VaadinIcon.MINUS.create();
            minusIcon.setColor("grey");
            Button decreaseButton = new Button(minusIcon);
            decreaseButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            decreaseButton.setTooltipText(translations.t("inventory.decrease"));
            decreaseButton.addClickListener(buttonClickEvent -> {
                new DecreaseInventoryDialog(inventoryService, translations, stockItemDto.getProduct(), v -> paginationView.loadCurrentPageAfterAddition()).open();
            });

            Icon warningIcon = VaadinIcon.WARNING.create();
            warningIcon.setColor("grey");

            Button reorderLevelEditButton = new Button(warningIcon);
            reorderLevelEditButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            reorderLevelEditButton.setTooltipText(translations.t("movement.editReorderLevel"));
            reorderLevelEditButton.addClickListener(buttonClickEvent -> {
                openReorderDialog(stockItemDto);
            });

            actionsLayout.add(increaseButton);
            actionsLayout.add(decreaseButton);
            actionsLayout.add(reorderLevelEditButton);
            actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            return actionsLayout;
        }
        )).setHeader("Action");

        return grid;
    }

    private void openReorderDialog(StockItemDto stockItemDto) {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(false);
        dialog.setWidth("90%");
        dialog.setMaxWidth("700px");
        dialog.setMinWidth("320px");
        dialog.setHeaderTitle("Reorder Settings: " + stockItemDto.getProduct().getName());

        BigDecimalField reorderField = new BigDecimalField("Reorder Level");
        reorderField.addValueChangeListener(e -> {
            BigDecimal v = e.getValue();
            boolean invalid = (v != null && v.compareTo(BigDecimal.ZERO) < 0);
            reorderField.setInvalid(invalid);
            reorderField.setErrorMessage("Must be 0 or greater");
        });

        reorderField.setWidthFull();

        TextField locationField = new TextField("Location");
        locationField.setWidthFull();

        // preload existing values

        reorderField.setValue(stockItemDto.getReorderLevel());
        locationField.setValue(stockItemDto.getLocation() == null ? "" : stockItemDto.getLocation());

        Button save = new Button("Save", e -> {
            inventoryService.updateReorderLevel(
                    stockItemDto.getId(),
                    reorderField.getValue(),
                    locationField.getValue()
            );
            dialog.close();
            NotificationSupport.showSuccess("Saved");
            paginationView.loadCurrentPageAfterAddition();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(cancel, save);
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setWidthFull();

        dialog.add(new VerticalLayout(reorderField, locationField, actions));
        dialog.open();
    }
}
