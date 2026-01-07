package com.muratoksuzer.vp.view;

import com.muratoksuzer.vp.config.RoleConstant;
import com.muratoksuzer.vp.entity.security.User;
import com.muratoksuzer.vp.service.SecurityService;
import com.muratoksuzer.vp.service.TranslationService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.Set;

@RolesAllowed({"USER", "ADMIN"})
public class MainLayout extends AppLayout {

    private TranslationService translations;
    private SecurityService securityService;

    public MainLayout(SecurityService securityService, TranslationService translations) {
        this.securityService = securityService;
        this.translations = translations;

        // Top navbar
        HorizontalLayout header = createHeader();
        addToNavbar(header);

        createDrawer();
    }

    private HorizontalLayout createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        Image logo = new Image("images/stock_inventory_100x.jpg", "MyStock");
        logo.setHeight("50px"); // <- small toolbar size
        RouterLink logoLink = new RouterLink();
        logoLink.setRoute(ProductView.class);      //
        logoLink.add(logo);

        H1 title = new H1("MyStock");
        title.getStyle().setMargin("0");
        RouterLink brand = new RouterLink();
        brand.setRoute(ProductView.class);      // <- target view
        brand.add(title);

        User currentUser = securityService.getCurrentUser();

        Span loggedUser = new Span(currentUser.getUsername());
        Avatar avatar = new Avatar(currentUser.getUsername());

        // Top right user menu
        Button logoutButton = new Button(translations.t("button.logout"), VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(s -> securityService.logout());

        HorizontalLayout userSection = new HorizontalLayout(loggedUser, avatar, logoutButton);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(toggle, logoLink, brand, userSection);

        header.setWidthFull();

        // Let brand stretch and push userSection to the right
        header.expand(brand);

        // Allow wrapping on small screens
        header.getStyle().set("flex-wrap", "wrap");


        userSection.getStyle().set("margin-left", "auto");

        return header;
    }

    private void createDrawer() {

        boolean hasAdminRole = hasAdminRole();

        SideNav nav = new SideNav();
        nav.setSizeFull();

        SideNavItem dashboardLink = new SideNavItem(translations.t("menu.dashboard"),
                DashboardView.class, VaadinIcon.DASHBOARD.create());

        SideNavItem productLink = new SideNavItem(
                translations.t("menu.product"),
                ProductView.class,
                VaadinIcon.PACKAGE.create()
        );

        SideNavItem inventoryLink = new SideNavItem(
                translations.t("menu.inventory"),
                InventoryView.class,
                VaadinIcon.CLIPBOARD_TEXT.create()
        );

        SideNavItem posLink = new SideNavItem(
                translations.t("menu.pos"),
                PosView.class,
                VaadinIcon.CART.create()
        );

        SideNavItem salesLink = new SideNavItem(
                translations.t("menu.sales"),
                SalesView.class,
                VaadinIcon.MONEY.create()
        );

        SideNavItem movementsLink = new SideNavItem(
                translations.t("menu.movements"),
                MovementsView.class,
                VaadinIcon.EXCHANGE.create()
        );


        SideNavItem settingsLink = new SideNavItem(
                translations.t("menu.settings"),
                SettingsView.class,
                VaadinIcon.COG.create()
        );

        SideNavItem usersLink = new SideNavItem(
                translations.t("menu.users"),
                UserManagementView.class,
                VaadinIcon.USERS.create()
        );


        SideNavItem aboutLink = new SideNavItem(
                translations.t("menu.about"),
                AboutView.class,
                VaadinIcon.INFO_CIRCLE.create()
        );

        nav.addItem(dashboardLink,
                productLink,
                inventoryLink,
                posLink,
                salesLink,
                movementsLink);

        if (hasAdminRole) {
            nav.addItem(usersLink);
        }

        nav.addItem(settingsLink,
                aboutLink
        );

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }

    private boolean hasAdminRole() {
        Set<String> currentUserRoles = securityService.getCurrentUserRoles();
        return currentUserRoles.contains(RoleConstant.ROLE_ADMIN);
    }
}
