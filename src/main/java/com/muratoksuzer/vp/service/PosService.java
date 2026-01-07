package com.muratoksuzer.vp.service;

import com.muratoksuzer.vp.entity.domain.Product;
import com.muratoksuzer.vp.entity.domain.Sale;
import com.muratoksuzer.vp.entity.domain.SaleLine;
import com.muratoksuzer.vp.entity.domain.StockItem;
import com.muratoksuzer.vp.entity.domain.StockMovement;
import com.muratoksuzer.vp.entity.domain.StockMovementType;
import com.muratoksuzer.vp.exception.AppLevelValidationException;
import com.muratoksuzer.vp.repository.ProductRepository;
import com.muratoksuzer.vp.repository.SaleLineRepository;
import com.muratoksuzer.vp.repository.SaleRepository;
import com.muratoksuzer.vp.repository.StockItemRepository;
import com.muratoksuzer.vp.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = Exception.class)
@Service
public class PosService {

    public record CartLine(Product product, BigDecimal quantity, BigDecimal unitPrice) {}

    private final ProductRepository productRepo;
    private final StockItemRepository stockRepo;
    private final StockMovementRepository movementRepo;
    private final SaleRepository saleRepo;
    private final SaleLineRepository saleLineRepo;
    private final TranslationService translations;

    public PosService(ProductRepository productRepo,
                      StockItemRepository stockRepo,
                      StockMovementRepository movementRepo,
                      SaleRepository saleRepo,
                      SaleLineRepository saleLineRepo,
                      TranslationService translations) {
        this.productRepo = productRepo;
        this.stockRepo = stockRepo;
        this.movementRepo = movementRepo;
        this.saleRepo = saleRepo;
        this.saleLineRepo = saleLineRepo;
        this.translations = translations;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Product findProductByBarcode(String barcode) {
        return productRepo.findByBarcode(barcode).orElse(null);
    }

    public Sale checkout(List<CartLine> cartLines) {
        if (cartLines == null || cartLines.isEmpty()) {
            throw new AppLevelValidationException(translations.t("pos.cart.empty"));
        }

        // 1) Validate stock
        for (CartLine line : cartLines) {
            StockItem stock = stockRepo.findByProductId(line.product().getId())
                    .orElseThrow(() -> new AppLevelValidationException(translations.t(
                            "pos.stock.missing",
                            line.product().getName()
                    )));

            if (stock.getQuantityOnHand().compareTo(line.quantity()) < 0) {
                throw new AppLevelValidationException(translations.t(
                        "pos.stock.insufficient",
                        line.product().getName(),
                        stock.getQuantityOnHand(),
                        line.quantity()
                ));
            }
        }

        // 2) Create sale header
        String saleNo = "S-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Sale sale = new Sale(saleNo);
        sale = saleRepo.save(sale);

        BigDecimal total = BigDecimal.ZERO;

        // 3) Create lines + update stock + movements
        for (CartLine line : cartLines) {
            BigDecimal lineTotal = line.unitPrice().multiply(line.quantity());
            total = total.add(lineTotal);

            SaleLine sl = new SaleLine(sale, line.product(), line.quantity(), line.unitPrice());
            sl.setLineTotal(lineTotal);
            saleLineRepo.save(sl);

            StockItem stock = stockRepo.findByProductId(line.product().getId()).orElseThrow();
            stock.setQuantityOnHand(stock.getQuantityOnHand().subtract(line.quantity()));
            stockRepo.save(stock);

            movementRepo.save(new StockMovement(
                    line.product(),
                    StockMovementType.OUT,
                    line.quantity(),
                    "Sale " + saleNo
            ));
        }

        sale.setTotal(total);
        return saleRepo.save(sale);
    }
}
