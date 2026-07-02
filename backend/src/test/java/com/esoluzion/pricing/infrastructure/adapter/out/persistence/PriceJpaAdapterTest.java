package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.esoluzion.pricing.domain.model.DateRange;
import com.esoluzion.pricing.domain.model.Price;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.entity.PriceEntity;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.mapper.PriceInfrastructureMapper;
import com.esoluzion.pricing.infrastructure.adapter.out.persistence.repository.PriceJpaRepository;

@ExtendWith(MockitoExtension.class)
class PriceJpaAdapterTest {

    @Mock
    private PriceJpaRepository jpaRepository;

    @Mock
    private PriceInfrastructureMapper mapper;

    @InjectMocks
    private PriceJpaAdapter adapter;

    @Test
    @DisplayName("Debe devolver Price cuando el repositorio encuentra resultados")
    void shouldReturnPriceWhenFound() {
        var entity = new PriceEntity();
        var domainPrice = new Price(1L, 35455L, 1, 0, BigDecimal.valueOf(35.50), "EUR",
                new DateRange(
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59)
                ));
        var page = new PageImpl<>(List.of(entity));

        when(jpaRepository.findApplicablePrice(any(), any(), any(), any(PageRequest.class)))
            .thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domainPrice);

        var result = adapter.findApplicablePrice(1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0));

        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(35.50));
        verify(jpaRepository).findApplicablePrice(1L, 35455L,
                LocalDateTime.of(2020, 6, 14, 10, 0), PageRequest.of(0, 1));
    }

    @Test
    @DisplayName("Debe devolver Optional vacío cuando no hay resultados")
    void shouldReturnEmptyWhenNotFound() {
        when(jpaRepository.findApplicablePrice(any(), any(), any(), any(PageRequest.class)))
            .thenReturn(Page.empty());

        var result = adapter.findApplicablePrice(1L, 35455L, LocalDateTime.of(2019, 1, 1, 0, 0));

        assertThat(result).isEmpty();
    }
}
