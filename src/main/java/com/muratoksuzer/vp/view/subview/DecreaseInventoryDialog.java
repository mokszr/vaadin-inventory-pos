package com.muratoksuzer.vp.view.subview;

import com.muratoksuzer.vp.dto.InventoryAdjustmentDto;
import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.entity.domain.ProductUnit;
import com.muratoksuzer.vp.service.InventoryService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DecreaseInventoryDialog extends Dialog {

    private final InventoryService inventoryService;
    private final TranslationService translations;

    private final Binder<InventoryAdjustmentDto> binder = new Binder<>(InventoryAdjustmentDto.class);
    private final InventoryAdjustmentDto bean = new InventoryAdjustmentDto();

    private final NumberField quantityField;
    private final TextArea noteField;

    public DecreaseInventoryDialog(
            InventoryService inventoryService,
            TranslationService translations,
            ProductDto product,
            Consumer<Void> onSuccessReload
    ) {
        this.inventoryService = inventoryService;
        this.translations = translations;

        setDraggable(true);
        setResizable(false);
        setWidth("90%");
        setMaxWidth("700px");
        setMinWidth("320px");

        bean.setProductId(product.getId());

        setHeaderTitle(translations.t("inventory.decreaseTitle", product.getName()));

        quantityField = new NumberField(translations.t("inventory.quantity"));
        quantityField.setWidthFull();
        quantityField.setMin(0);
        quantityField.setStep(1);

        List<ProductUnit> countableUnits = Arrays.asList(ProductUnit.PCS, ProductUnit.BOX, ProductUnit.SET, ProductUnit.PACK);

        if (!countableUnits.contains(product.getUnit())) {
            quantityField.setStep(0.01);
        }

        quantityField.setClearButtonVisible(true);

        binder.forField(quantityField)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("inventory.quantity")))
                .withValidator(v -> v > 0, translations.t("validation.mustBePositive"))
                .bind(
                        dto -> dto.getQuantity() == null ? null : dto.getQuantity().doubleValue(),
                        (dto, v) -> dto.setQuantity(v == null ? null : BigDecimal.valueOf(v))
                );

        noteField = new TextArea(translations.t("inventory.note"));
        noteField.setWidthFull();
        noteField.setMaxLength(512);
        noteField.setClearButtonVisible(true);

        binder.bind(noteField, InventoryAdjustmentDto::getNote, InventoryAdjustmentDto::setNote);

        Span hint = new Span(translations.t("inventory.decreaseHint"));
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button cancel = new Button(translations.t("button.cancel"), e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickShortcut(Key.ESCAPE);

        Button save = new Button(translations.t("inventory.decreaseButton"));
        save.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        save.addClickShortcut(Key.ENTER);
        save.addClickListener(e -> {
            if (saveAndApplyDecrease(product)) {
                close();
                if (onSuccessReload != null) onSuccessReload.accept(null);
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, save);
        buttons.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout content = new VerticalLayout(quantityField, noteField, hint, buttons);
        content.setPadding(false);
        content.setSpacing(true);

        add(content);

        binder.readBean(bean);
        quantityField.focus();
    }

    private boolean saveAndApplyDecrease(ProductDto product) {
        try {
            binder.writeBean(bean);

            // If you throw a custom exception like InsufficientStockException, catch it below.
            inventoryService.decreaseStock(bean.getProductId(), bean.getQuantity(), bean.getNote());

            NotificationSupport.showSuccess(
                    translations.t("inventory.decreasedNotification", product.getName(), bean.getQuantity().toPlainString())
            );
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        } catch (RuntimeException ex) {
            if (ex.getMessage().equals(translations.t("inventory.insufficientStock"))) {
                NotificationSupport.showError(translations.t("inventory.insufficientStock"));
                return false;
            }
            NotificationSupport.showError(translations.t("general.unexpectedError"));
            return false;
        }
    }
}
