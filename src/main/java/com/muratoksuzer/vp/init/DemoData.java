package com.muratoksuzer.vp.init;

import com.muratoksuzer.vp.dto.ProductDto;
import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.entity.domain.Sale;
import com.muratoksuzer.vp.entity.domain.SaleLine;
import com.muratoksuzer.vp.repository.ProductRepository;
import com.muratoksuzer.vp.repository.SaleLineRepository;
import com.muratoksuzer.vp.repository.SaleRepository;
import com.muratoksuzer.vp.service.InventoryService;
import com.muratoksuzer.vp.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Configuration
public class DemoData {


    @Bean
    CommandLineRunner init(
            ProductService productService,
            ProductRepository productRepository,
            InventoryService inventoryService,
            SaleRepository saleRepository,
            SaleLineRepository saleLineRepository
    ) {
        return args -> {

            // ---------- PRODUCTS + STOCK ----------
            if (productRepository.count() == 0) {
                Product p1 = productService.saveProduct(new ProductDto("Fresh Orange Juice 250ml", "869000000001"));
                Product p2 = productService.saveProduct(new ProductDto("Water 500ml", "869000000002"));
                Product p3 = productService.saveProduct(new ProductDto("Chips 50g", "869000000003"));

                inventoryService.increaseStock(p1.getId(), new BigDecimal("50"), "initial");
                inventoryService.increaseStock(p2.getId(), new BigDecimal("80"), "initial");
                inventoryService.increaseStock(p3.getId(), new BigDecimal("40"), "initial");
            }

            // ---------- SALES + SALE LINES (LAST 14 DAYS) ----------
            if (saleRepository.count() == 0 && saleLineRepository.count() == 0) {
                seedLast14DaysSales(productRepository.findAll(), saleRepository, saleLineRepository);
            }
        };
    }

    /**
     * Creates sales + lines for last 14 days so "Top Products" chart has meaningful data.
     * Uses native update to override date_created (because @CreationTimestamp may override insert-time values).
     */
    @Transactional
    void seedLast14DaysSales(
            List<Product> products,
            SaleRepository saleRepository,
            SaleLineRepository saleLineRepository
    ) {
        if (products == null || products.isEmpty()) {
            return;
        }

        ZoneId zone = ZoneId.of("Europe/Istanbul");
        Random rnd = new Random(42); // deterministic

        // Fixed "demo pricing" (keeps chart stable)
        BigDecimal orangeJuicePrice = new BigDecimal("35.00");
        BigDecimal waterPrice = new BigDecimal("12.50");
        BigDecimal chipsPrice = new BigDecimal("28.00");

        Product p1 = products.get(0);
        Product p2 = products.size() > 1 ? products.get(1) : products.get(0);
        Product p3 = products.size() > 2 ? products.get(2) : products.get(0);

        for (int daysAgo = 13; daysAgo >= 0; daysAgo--) {
            LocalDate day = LocalDate.now(zone).minusDays(daysAgo);

            // 1–4 sales per day
            int salesPerDay = 1 + rnd.nextInt(4);

            // Make weekends a bit more active (nicer chart)
            DayOfWeek dow = day.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                salesPerDay += 1;
            }

            for (int i = 0; i < salesPerDay; i++) {
                OffsetDateTime saleTime = day
                        .atTime(10 + rnd.nextInt(10), rnd.nextInt(60))
                        .atZone(zone)
                        .toOffsetDateTime();

                Sale sale = new Sale();
                sale.setSaleNo("S-" + day + "-" + (i + 1));

                // Save first so we have an ID
                Sale savedSale = saleRepository.saveAndFlush(sale);

                // Create 1–3 sale lines
                int lineCount = 1 + rnd.nextInt(3);
                BigDecimal saleTotal = BigDecimal.ZERO;

                for (int li = 0; li < lineCount; li++) {
                    int pick = rnd.nextInt(3);

                    Product chosen;
                    BigDecimal unitPrice;

                    // Bias the sales so Top Products chart looks interesting:
                    // - More Orange Juice
                    // - Then Water
                    // - Then Chips
                    int bias = rnd.nextInt(100);
                    if (bias < 50) {
                        pick = 0; // Orange Juice
                    } else if (bias < 80) {
                        pick = 1; // Water
                    } else {
                        pick = 2; // Chips
                    }

                    if (pick == 0) {
                        chosen = p1;
                        unitPrice = orangeJuicePrice;
                    } else if (pick == 1) {
                        chosen = p2;
                        unitPrice = waterPrice;
                    } else {
                        chosen = p3;
                        unitPrice = chipsPrice;
                    }

                    // Qty: mostly 1-3
                    BigDecimal qty = switch (rnd.nextInt(5)) {
                        case 0 -> new BigDecimal("1");
                        case 1 -> new BigDecimal("2");
                        case 2 -> new BigDecimal("3");
                        case 3 -> new BigDecimal("1");
                        default -> new BigDecimal("1");
                    };

                    BigDecimal lineTotal = unitPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);
                    saleTotal = saleTotal.add(lineTotal);

                    SaleLine line = new SaleLine();
                    line.setSale(savedSale);
                    line.setProduct(chosen);
                    line.setQuantity(qty);
                    line.setUnitPrice(unitPrice);
                    line.setLineTotal(lineTotal);

                    saleLineRepository.save(line);
                }

                // Update sale total
                savedSale.setTotal(saleTotal);
                saleRepository.save(savedSale);

                // Override dateCreated (reliable) using native UPDATE
                saleRepository.updateDateCreatedNative(savedSale.getId(), saleTime);
            }
        }
    }
}
