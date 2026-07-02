package com.esoluzion.pricing.application;

import com.esoluzion.pricing.application.model.PriceQuery;
import com.esoluzion.pricing.application.model.PriceResult;
import com.esoluzion.pricing.application.port.out.PriceRepositoryPort;
import com.esoluzion.pricing.application.service.GetApplicablePriceService;
import com.esoluzion.pricing.domain.exception.PriceNotFoundException;
import com.esoluzion.pricing.domain.model.DateRange;
import com.esoluzion.pricing.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetApplicablePriceService")
class GetApplicablePriceServiceTest {

    @Mock
    private PriceRepositoryPort priceRepository;

    private GetApplicablePriceService service;

    @Captor
    private ArgumentCaptor<Long> brandIdCaptor;

    @Captor
    private ArgumentCaptor<Long> productIdCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> applicationDateCaptor;

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

    @BeforeEach
    void setUp() {
        service = new GetApplicablePriceService(priceRepository);
    }

    @Nested
    @DisplayName("when price is found")
    class WhenPriceFound {

        private Price domainPrice;

        @BeforeEach
        void setUp() {
            var startDate = LocalDateTime.of(2020, 6, 14, 0, 0, 0);
            var endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
            var dateRange = new DateRange(startDate, endDate);

            domainPrice = new Price(
                    BRAND_ID, PRODUCT_ID, 1, 0,
                    new BigDecimal("35.50"), "EUR", dateRange
            );

            when(priceRepository.findApplicablePrice(any(), any(), any()))
                    .thenReturn(Optional.of(domainPrice));
        }

        @Test
        @DisplayName("should return PriceResult when repository finds price")
        void shouldReturnPriceResult() {
            var query = new PriceQuery(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
            var result = service.getApplicablePrice(query);

            assertAll(
                    () -> assertEquals(PRODUCT_ID, result.productId()),
                    () -> assertEquals(BRAND_ID, result.brandId()),
                    () -> assertEquals(1, result.priceList()),
                    () -> assertEquals(new BigDecimal("35.50"), result.price()),
                    () -> assertEquals("EUR", result.currency()),
                    () -> assertEquals(domainPrice.getDateRange().getStartDate(), result.startDate()),
                    () -> assertEquals(domainPrice.getDateRange().getEndDate(), result.endDate())
            );
        }

        @Test
        @DisplayName("should map Price domain to PriceResult correctly")
        void shouldMapDomainToResultCorrectly() {
            var query = new PriceQuery(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
            var result = service.getApplicablePrice(query);

            assertAll(
                    () -> assertInstanceOf(PriceResult.class, result),
                    () -> assertEquals(domainPrice.getProductId(), result.productId()),
                    () -> assertEquals(domainPrice.getBrandId(), result.brandId()),
                    () -> assertEquals(domainPrice.getPriceList(), result.priceList()),
                    () -> assertEquals(domainPrice.getPrice(), result.price()),
                    () -> assertEquals(domainPrice.getCurrency(), result.currency()),
                    () -> assertEquals(domainPrice.getDateRange().getStartDate(), result.startDate()),
                    () -> assertEquals(domainPrice.getDateRange().getEndDate(), result.endDate())
            );
        }

        @Test
        @DisplayName("should call repository with correct parameters")
        void shouldCallRepositoryWithCorrectParameters() {
            var query = new PriceQuery(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
            service.getApplicablePrice(query);

            verify(priceRepository).findApplicablePrice(
                    brandIdCaptor.capture(),
                    productIdCaptor.capture(),
                    applicationDateCaptor.capture()
            );

            assertAll(
                    () -> assertEquals(BRAND_ID, brandIdCaptor.getValue()),
                    () -> assertEquals(PRODUCT_ID, productIdCaptor.getValue()),
                    () -> assertEquals(APPLICATION_DATE, applicationDateCaptor.getValue())
            );
        }
    }

    @Nested
    @DisplayName("when price is not found")
    class WhenPriceNotFound {

        @BeforeEach
        void setUp() {
            when(priceRepository.findApplicablePrice(any(), any(), any()))
                    .thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("should throw PriceNotFoundException when repository finds no price")
        void shouldThrowPriceNotFoundException() {
            var query = new PriceQuery(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

            assertThrows(PriceNotFoundException.class, () ->
                    service.getApplicablePrice(query)
            );
        }
    }
}
