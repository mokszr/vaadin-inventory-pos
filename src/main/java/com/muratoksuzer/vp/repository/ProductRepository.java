package com.muratoksuzer.vp.repository;

import com.muratoksuzer.vp.entity.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);

    Product findByNameIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

}
