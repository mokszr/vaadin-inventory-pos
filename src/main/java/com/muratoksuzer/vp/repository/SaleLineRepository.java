package com.muratoksuzer.vp.repository;


import com.muratoksuzer.vp.dto.TopProductPoint;
import com.muratoksuzer.vp.entity.domain.SaleLine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SaleLineRepository extends JpaRepository<SaleLine, Long> {

    @Query("""
        select new com.muratoksuzer.vp.dto.TopProductPoint(
            sl.product.name,
            coalesce(sum(sl.lineTotal), 0)
        )
        from SaleLine sl
        where sl.sale.dateCreated >= :from and sl.sale.dateCreated < :to
        group by sl.product.id, sl.product.name
        order by coalesce(sum(sl.lineTotal), 0) desc
    """)
    List<TopProductPoint> topProductsByRevenue(@Param("from") OffsetDateTime from,
                                               @Param("to") OffsetDateTime to,
                                               Pageable pageable);
}
