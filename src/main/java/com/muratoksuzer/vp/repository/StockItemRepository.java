package com.muratoksuzer.vp.repository;

import com.muratoksuzer.vp.dto.LowStockRow;
import com.muratoksuzer.vp.entity.domain.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByProductId(Long productId);

    Page<StockItem> findByProductNameContainingIgnoreCaseOrProductBarcodeContaining(String nameSearchTerm, String barcodeSearchTerm, Pageable pageable);

    @Query("""
        select new com.muratoksuzer.vp.dto.LowStockRow(
            si.product.name,
            si.quantityOnHand,
            si.reorderLevel,
            (si.reorderLevel - si.quantityOnHand)
        )
        from StockItem si
        where si.quantityOnHand <= si.reorderLevel
        order by (si.reorderLevel - si.quantityOnHand) desc
    """)
    List<LowStockRow> findLowStock(Pageable pageable);

    @Query("""
        select count(si)
        from StockItem si
        where si.quantityOnHand <= si.reorderLevel
    """)
    long countLowStock();
}
