package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.entity.domain.ProductUnit;
import com.muratoksuzer.vp.service.ProductService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.muratoksuzer.vp.view.components.PaginationView;
import com.muratoksuzer.vp.view.components.SearchBarView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

@Route(value = "products", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Products")
@RolesAllowed({"USER", "ADMIN"})
public class ProductView extends VerticalLayout {

    private final ProductService productService;
    private final TranslationService translations;
    private Binder<ProductDto> binder;
    private PaginationView paginationView;

    private ProductDto currentProduct;
    private TextField nameField;
    private TextField barcodeField;
    private Button saveButton;
    private Button cancelButton;
    private Grid<ProductDto> productGrid;
    private ComboBox<ProductUnit> unitComboBox;

    public ProductView(ProductService productService, TranslationService translations) {
        this.productService = productService;
        this.translations = translations;
        setSizeFull();

        add(new H2(translations.t("product.yourProducts")));
        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        createForm();

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

        Grid<ProductDto> grid = createGrid();

        add(grid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<ProductDto> termPage = productService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                productGrid.setItems(termPage.getContent());
                return termPage;
            }
        };
        paginationView.setCurrentPageKey("currentPageProduct");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();

        Button addNewButton = new Button(translations.t("button.add"));

        addNewButton.addClickListener(ev -> openAddDialog(new ProductDto()));

        addNewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        horizontalLayout.add(addNewButton);
        return horizontalLayout;
    }

    private void openAddDialog(ProductDto aProductDto) {
        currentProduct = aProductDto;
        binder.readBean(currentProduct);

        Dialog dialog = new Dialog();
        dialog.setDraggable(true);
        dialog.setResizable(false);
        dialog.setWidth("90%");
        dialog.setMaxWidth("1000px");
        dialog.setMinWidth("300px");
        dialog.setHeaderTitle(translations.t("product.addProduct"));

        saveButton = new Button(translations.t("button.save"));
        cancelButton = new Button(translations.t("button.cancel"));

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(ev -> {
            dialog.close();
        });

        saveButton.addClickListener(ev -> {
            boolean saved = saveProduct();
            if (saved) {
                dialog.close();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(nameField, barcodeField, unitComboBox);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout, buttons);
        dialog.open();
    }

    private void createForm() {

        nameField = new TextField(translations.t("product.name"));
        nameField.setWidthFull();

        binder = new Binder<>(ProductDto.class);
        binder.forField(nameField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("product.name")))
                .bind(
                        ProductDto::getName,
                        ProductDto::setName);

        barcodeField = new TextField(translations.t("product.barcode"));
        barcodeField.setWidthFull();


        binder.forField(barcodeField)
                .withValidator(StringUtils::isNotBlank, translations.t("validation.fieldCannotBeBlank", translations.t("product.barcode")))
                .bind(
                        ProductDto::getBarcode,
                        ProductDto::setBarcode);

        unitComboBox = new ComboBox<>(translations.t("product.unit"));
        unitComboBox.setItems(ProductUnit.values());
        unitComboBox.setItemLabelGenerator(this::translateUnitType);
        unitComboBox.setWidthFull();

        binder.forField(unitComboBox)
                .withValidator(Objects::nonNull, translations.t("validation.fieldCannotBeBlank", translations.t("product.unit")))
                .bind(
                        ProductDto::getUnit,
                        ProductDto::setUnit
                );
    }

    private Grid<ProductDto> createGrid() {
        productGrid = new Grid<>(ProductDto.class, false);
        productGrid.setSizeFull();
        productGrid.setEmptyStateText(translations.t("general.noItems"));

        productGrid.addColumn(ProductDto::getBarcode).setHeader(translations.t("product.barcode")).setAutoWidth(true);
        productGrid.addColumn(ProductDto::getName).setHeader(translations.t("product.name")).setFlexGrow(1);
        productGrid.addColumn(new ComponentRenderer<>(productDto -> {
            Span span = new Span();
            span.setText(translateUnitType(productDto.getUnit()));
            return span;
        })).setHeader(translations.t("product.unit")).setAutoWidth(true);
        productGrid.addColumn(ProductDto::isActive).setHeader(translations.t("product.active")).setAutoWidth(true);

        productGrid.addColumn(new ComponentRenderer<>(productDto -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setPadding(false);
            RouterLink routerLink = new RouterLink(PriceView.class, productDto.getId());
            routerLink.add(VaadinIcon.MONEY.create());
            routerLink.getElement().setAttribute("title", translations.t("product.action.prices"));
            Div linkWrapper = new Div(routerLink);
            linkWrapper.addClickListener(event -> {
                // Additional actions before navigation
                paginationView.setCurrentPageInSession();
            });

            Icon deleteIcon = VaadinIcon.TRASH.create();
            deleteIcon.setColor("grey");

            Button deleteButton = new Button(deleteIcon);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            deleteButton.setTooltipText(translations.t("button.delete"));
            deleteButton.addClickListener(buttonClickEvent -> {
                deleteProductConfirmationDialog(productDto);
            });

            Icon editIcon = VaadinIcon.EDIT.create();
            editIcon.setColor("grey");
            Button editButton = new Button(editIcon);
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
            editButton.setTooltipText(translations.t("button.edit"));
            editButton.addClickListener(buttonClickEvent -> {
                openAddDialog(productDto);
            });

            actionsLayout.add(deleteButton);
            actionsLayout.add(editButton);
            actionsLayout.add(linkWrapper);
            actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            return actionsLayout;
        }
        ));

        return productGrid;
    }

    private void deleteProductConfirmationDialog(ProductDto product) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(translations.t("product.deleteProductTitle"));
        dialog.setText(
                translations.t("product.deleteProductMessage", product.getName()));

        dialog.setCancelable(true);
        dialog.setCancelText(translations.t("button.cancel"));

        dialog.setConfirmText(translations.t("button.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            productService.delete(product);
            paginationView.loadCurrentPageAfterRemove();
            NotificationSupport.showSuccess(translations.t("notification.isDeleted", product.getName()));
        });

        dialog.open();
    }

    private boolean saveProduct() {
        try {
            binder.writeBean(currentProduct);

            productService.saveProduct(currentProduct);
            paginationView.loadCurrentPageAfterAddition();

            NotificationSupport.showSuccess(translations.t("notification.isSaved", translations.t("product.entityName")));
            return true;
        } catch (ValidationException ve) {
            ve.getValidationErrors().stream().forEach(v -> NotificationSupport.showError(v.getErrorMessage()));
            return false;
        }
    }

    private String translateUnitType(ProductUnit unitType) {
        if (unitType == null) {
            return "";
        }
        return translations.t("ProductUnit." + unitType);
    }

}
