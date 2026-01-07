package com.muratoksuzer.vp.repository;

import com.muratoksuzer.vp.dto.DailyTotalPoint;
import com.muratoksuzer.vp.entity.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;


public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findBySaleNoContainingIgnoreCase(String searchTerm, Pageable pageable);


    @Query("""
        select coalesce(sum(s.total), 0)
        from Sale s
        where s.dateCreated >= :from and s.dateCreated < :to
    """)
    BigDecimal sumTotalBetween(@Param("from") OffsetDateTime from,
                               @Param("to") OffsetDateTime to);

    @Query("""
        select count(s)
        from Sale s
        where s.dateCreated >= :from and s.dateCreated < :to
    """)
    long countBetween(@Param("from") OffsetDateTime from,
                      @Param("to") OffsetDateTime to);

    @Query("""
    select function('date', s.dateCreated), sum(s.total)
    from Sale s
    where s.dateCreated >= :from and s.dateCreated < :to
    group by function('date', s.dateCreated)
    order by function('date', s.dateCreated)
""")
    List<Object[]> dailyTotalsRaw(@Param("from") OffsetDateTime from,
                                  @Param("to") OffsetDateTime to);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE sale SET date_created = :dt WHERE id = :id",
            nativeQuery = true
    )
    int updateDateCreatedNative(@Param("id") Long id, @Param("dt") OffsetDateTime dt);
}
