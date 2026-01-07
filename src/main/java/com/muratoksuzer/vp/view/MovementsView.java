package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.StockMovementDto;
import com.muratoksuzer.vp.service.StockMovementService;
import com.muratoksuzer.vp.service.TranslationService;
import com.muratoksuzer.vp.support.NotificationSupport;
import com.muratoksuzer.vp.view.components.PaginationView;
import com.muratoksuzer.vp.view.components.SearchBarView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Route(value = "movements", layout = MainLayout.class)
@PageTitle("Stock Movements")
@RolesAllowed({"USER", "ADMIN"})
public class MovementsView extends VerticalLayout {

    private PaginationView paginationView;
    private final TranslationService translations;
    private StockMovementService stockMovementService;
    private Grid<StockMovementDto> stockMovementGrid;

    public MovementsView(StockMovementService stockMovementService, TranslationService translations) {
        this.stockMovementService = stockMovementService;
        this.translations = translations;
        setSizeFull();
        add(new H2(translations.t("movement.title")));

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

        this.stockMovementGrid = createGrid();
        add(stockMovementGrid);
        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<StockMovementDto> page = stockMovementService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                stockMovementGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPageMovement");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());

    }

    private Grid<StockMovementDto> createGrid() {
        Grid<StockMovementDto> grid = new Grid<>(StockMovementDto.class, false);
        grid.setSizeFull();
        grid.setEmptyStateText(translations.t("general.noItems"));

        grid.addColumn(m -> m.getProduct().getName()).setHeader("Product").setFlexGrow(1);
        grid.addColumn(si -> si.getProduct().getBarcode()).setHeader("Barcode").setAutoWidth(true);
        grid.addColumn(StockMovementDto::getType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(StockMovementDto::getQuantity).setHeader("Quantity").setFlexGrow(1);
        grid.addColumn(StockMovementDto::getDateCreated).setHeader("Date").setFlexGrow(1);
        grid.addColumn(StockMovementDto::getNote).setHeader("Note").setFlexGrow(1);

        return grid;
    }
}
