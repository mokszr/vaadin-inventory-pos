package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.dto.SaleDto;
import com.muratoksuzer.vp.service.SaleService;
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

@Route(value = "sales", layout = MainLayout.class)
@PageTitle("Sales")
@RolesAllowed({"USER", "ADMIN"})
public class SalesView extends VerticalLayout {

    private final SaleService saleService;
    private PaginationView paginationView;
    private final TranslationService translations;
    private Grid<SaleDto> saleGrid;

    public SalesView(SaleService saleService, TranslationService translations) {
        this.saleService = saleService;
        this.translations = translations;
        setSizeFull();
        add(new H2(translations.t("sale.title")));

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

        this.saleGrid = createGrid();
        add(saleGrid);

        paginationView = new PaginationView() {

            @Override
            protected Page loadAndGetPage(Pageable pageable, String searchTerm) {
                Page<SaleDto> page = saleService.findPaginated(pageable, searchTerm);
                // Update the grid with the current page data
                saleGrid.setItems(page.getContent());
                return page;
            }
        };
        paginationView.setCurrentPageKey("currentPageMovement");
        add(paginationView);
        paginationView.loadPage(paginationView.getCurrentPageInSession());

    }

    private Grid<SaleDto> createGrid() {
        Grid<SaleDto> grid = new Grid<>(SaleDto.class, false);
        grid.setSizeFull();
        grid.setEmptyStateText(translations.t("general.noItems"));


        grid.addColumn(SaleDto::getSaleNo).setHeader("Sale No").setAutoWidth(true);
        grid.addColumn(SaleDto::getDateCreated).setHeader("Created At").setAutoWidth(true);
        grid.addColumn(SaleDto::getTotal).setHeader("Total").setAutoWidth(true);

        return grid;
    }
}
