package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

import com.esoluzion.pricing.application.port.out.PriceRepositoryPort;
import com.esoluzion.pricing.domain.model.Price;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.mapper.PriceInfrastructureMapper;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.repository.PriceJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PriceJpaAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository jpaRepository;
    private final PriceInfrastructureMapper mapper;

    public PriceJpaAdapter(PriceJpaRepository jpaRepository, PriceInfrastructureMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        return jpaRepository.findApplicablePrice(brandId, productId, applicationDate, PageRequest.of(0, 1))
            .getContent().stream()
            .findFirst()
            .map(mapper::toDomain);
    }
}
