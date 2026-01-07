package com.muratoksuzer.vp.repository;

import com.muratoksuzer.vp.entity.domain.StockMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductNameContainingIgnoreCaseOrProductBarcodeContaining(String nameSearchTerm, String barcodeSearchTerm, Pageable pageable);

}
