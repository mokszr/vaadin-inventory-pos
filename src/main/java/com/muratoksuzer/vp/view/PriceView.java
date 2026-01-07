package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.PriceDto;
import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.entity.domain.Currency;
import com.muratoksuzer.vp.service.PriceService;
import com.muratoksuzer.vp.service.ProductService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.muratoksuzer.vp.view.components.PaginationView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route(value = "prices", layout = MainLayout.class)
@PageTitle("Prices")
@RolesAllowed({"USER", "ADMIN"})
public class PriceView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PriceService priceService;
    private final ProductService productService;
    private final TranslationService translations;
    private final PaginationView paginationView;

    private Grid<PriceDto> priceGrid;

    private Span productName;
    private Binder<PriceDto> binder;
    private PriceDto currentPrice;

    private NumberField amountField;
    private ComboBox<Currency> currencyComboBox;
    private Checkbox activeCheckbox;

    private Button saveButton;
    private Button cancelButton;
    private ProductDto currentProduct;

    public PriceView(PriceService priceService, ProductService productService, TranslationService translations) {
        this.priceService = priceService;
        this.productService = productService;
        this.translations = translations;

        setSizeFull();

        add(new H2(translations.t("price.productPrices")));

        productName = new Span("");
        add(productName);
        add(createToolbar());
        createForm();
        add(createGrid());

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<PriceDto> page = priceService.findPaginated(currentProduct.getId(), pageable);
                // Update the grid with the current page data
                priceGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPagePrice");
        add(paginationView);

    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button addPriceButton = new Button(translations.t("price.addPrice"));
        addPriceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addPriceButton.addClickListener(e -> {
            if (currentProduct == null) {
                NotificationSupport.showError(translations.t("price.selectProductFirst"));
                return;
            }
            PriceDto dto = new PriceDto();
            dto.setProductId(currentProduct.getId());
            dto.setCurrency(Currency.TRY);
            dto.setActive(true);
            openAddDialog(dto);
        });

        toolbar.add(addPriceButton);
        return toolbar;
    }

    private void createForm() {
        binder = new Binder<>(PriceDto.class);

        amountField = new NumberField(translations.t("price.amount"));
        amountField.setWidthFull();
        amountField.setMin(0);
        amountField.setStep(0.01);
        amountField.setClearButtonVisible(true);

        // NumberField is Double-based; we convert to BigDecimal via binder
        binder.forField(amountField)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("price.amount")))
                .withValidator(v -> v >= 0, translations.t("validation.mustBePositive"))
                .bind(
                        dto -> dto.getAmount() == null ? null : dto.getAmount().doubleValue(),
                        (dto, v) -> dto.setAmount(v == null ? null : BigDecimal.valueOf(v))
                );

        currencyComboBox = new ComboBox<>(translations.t("price.currency"));
        currencyComboBox.setWidthFull();
        currencyComboBox.setItems(Currency.values());
        currencyComboBox.setItemLabelGenerator(this::translateCurrency);

        binder.forField(currencyComboBox)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("price.currency")))
                .bind(PriceDto::getCurrency, PriceDto::setCurrency);


        activeCheckbox = new Checkbox(translations.t("price.activePrice"));
        binder.bind(activeCheckbox, PriceDto::isActive, PriceDto::setActive);
    }

    private Grid<PriceDto> createGrid() {
        priceGrid = new Grid<>(PriceDto.class, false);
        priceGrid.setSizeFull();
        priceGrid.setEmptyStateText(translations.t("general.noItems"));

        priceGrid.addColumn(p -> p.getAmount() == null ? "" : p.getAmount().toPlainString())
                .setHeader(translations.t("price.amount"))
                .setAutoWidth(true);

        priceGrid.addColumn(p -> translateCurrency(p.getCurrency()))
                .setHeader(translations.t("price.currency"))
                .setAutoWidth(true);


        priceGrid.addColumn(new ComponentRenderer<>(p -> {
            Span s = new Span(p.isActive() ? translations.t("general.yes") : translations.t("general.no"));
            if (p.isActive()) {
                s.getElement().getThemeList().add("badge success");
            } else {
                s.getElement().getThemeList().add("badge contrast");
            }
            return s;
        })).setHeader(translations.t("price.active"))
          .setAutoWidth(true);

        priceGrid.addColumn(new ComponentRenderer<>(priceDto -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setPadding(false);
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            // Activate button (only if not active)
            Icon activateIcon = VaadinIcon.EDIT.create();
            activateIcon.setColor("grey");
            Button activateBtn = new Button(activateIcon);
            activateBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            activateBtn.setTooltipText(translations.t("price.makeActive"));
            activateBtn.setVisible(!priceDto.isActive());
            activateBtn.addClickListener(e -> activatePriceConfirmationDialog(priceDto));

            // Delete button (optional; you may want to forbid deleting active price)
            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("grey");
            Button deleteBtn = new Button(deleteIcon);
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            deleteBtn.setTooltipText(translations.t("button.delete"));
            deleteBtn.addClickListener(e -> deletePriceConfirmationDialog(priceDto));

            actions.add(activateBtn, deleteBtn);
            return actions;
        })).setAutoWidth(true);

        return priceGrid;
    }

    private void openAddDialog(PriceDto dto) {
        currentPrice = dto;
        binder.readBean(currentPrice);

        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(false);
        dialog.setWidth("90%");
        dialog.setMaxWidth("700px");
        dialog.setMinWidth("300px");

        String productName = currentProduct.getName();
        dialog.setHeaderTitle(translations.t("price.addPriceFor", productName));

        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        cancelButton.addClickListener(ev -> dialog.close());
        saveButton.addClickListener(ev -> {
            boolean saved = savePrice();
            if (saved) {
                dialog.close();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout form = new VerticalLayout(amountField, currencyComboBox, activeCheckbox);
        form.setPadding(false);
        form.setSpacing(true);

        // UX hint: active checkbox implies "deactivate previous active"
        Span hint = new Span(translations.t("price.activeHint"));
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
        form.add(hint);

        dialog.add(form, buttons);
        dialog.open();
    }

    private boolean savePrice() {
        try {
            binder.writeBean(currentPrice);

            if (currentPrice.getProductId() == null && currentProduct != null) {
                currentPrice.setProductId(currentProduct.getId());
            }

            // This should handle:
            // - insert new price row
            // - if currentPrice.active=true => deactivate old active and activate this one
            priceService.createPriceAndSwitchActiveIfRequested(currentPrice, currentProduct.getId());

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("price.entityName")));
            reloadPricesForSelectedProduct();
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    private void activatePriceConfirmationDialog(PriceDto priceDto) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("price.activateTitle"));
        dialog.setText(translations.t("price.activateMessage"));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.confirm"));
        dialog.setConfirmButtonTheme("primary");
        dialog.addConfirmListener(event -> {
            priceService.activatePrice(priceDto.getId(), currentProduct.getId());
            NotificationSupport.showSuccess(translations.t("price.activated"));
            reloadPricesForSelectedProduct();
        });

        dialog.open();
    }

    private void deletePriceConfirmationDialog(PriceDto priceDto) {
        if (priceDto.isActive()) {
            NotificationSupport.showError(translations.t("price.cannotDeleteActive"));
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("price.deleteTitle"));
        dialog.setText(translations.t("price.deleteMessage"));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            priceService.delete(priceDto.getId());
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", translations.t("price.entityName")));
            reloadPricesForSelectedProduct();
        });

        dialog.open();
    }

    private void reloadPricesForSelectedProduct() {
        paginationView.loadPage(0);
    }

    private String translateCurrency(Currency currency) {
        if (currency == null) return "";
        return translations.t("Currency." + currency.name());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long productId) {
        currentProduct = productService.getProduct(productId);
        if(currentProduct == null) {
            productName.setText(translations.t("price.noProductFound"));
            return;
        }

        productName.setText(currentProduct.getName());
        paginationView.loadPage(0);
    }
}
