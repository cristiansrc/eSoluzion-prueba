package com.esoluzion.pricing.infrastructure.adapter.out.persistence.repository;

import com.esoluzion.pricing.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
        SELECT p FROM PriceEntity p
        WHERE p.brandId = :brandId
          AND p.productId = :productId
          AND p.startDate <= :applicationDate
          AND p.endDate >= :applicationDate
        ORDER BY p.priority DESC
        """)
    Page<PriceEntity> findApplicablePrice(
        @Param("brandId") Long brandId,
        @Param("productId") Long productId,
        @Param("applicationDate") LocalDateTime applicationDate,
        Pageable pageable
    );
}
