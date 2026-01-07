package com.muratoksuzer.vp.repository;

import com.muratoksuzer.vp.entity.domain.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Page<Price> findByProductIdOrderByActiveDesc(Long productId, Pageable pageable);

    Price findByProductIdAndActive(Long productId, boolean active);
}
