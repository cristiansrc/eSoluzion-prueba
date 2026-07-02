package com.esoluzion.pricing.domain;

import com.esoluzion.pricing.domain.model.DateRange;
import com.esoluzion.pricing.domain.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Price domain entity")
class PriceTest {

    private final LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 0, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
    private final DateRange validRange = new DateRange(startDate, endDate);

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create Price with all fields")
        void shouldCreatePriceWithAllFields() {
            var price = new Price(
                    1L, 35455L, 1, 0,
                    new BigDecimal("35.50"), "EUR", validRange
            );

            assertAll(
                    () -> assertEquals(1L, price.getBrandId()),
                    () -> assertEquals(35455L, price.getProductId()),
                    () -> assertEquals(1, price.getPriceList()),
                    () -> assertEquals(0, price.getPriority()),
                    () -> assertEquals(new BigDecimal("35.50"), price.getPrice()),
                    () -> assertEquals("EUR", price.getCurrency()),
                    () -> assertEquals(validRange, price.getDateRange())
            );
        }

        @Test
        @DisplayName("should reject negative price")
        void shouldRejectNegativePrice() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Price(1L, 35455L, 1, 0,
                            new BigDecimal("-1.00"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject zero price (valid: price >= 0)")
        void shouldAcceptZeroPrice() {
            assertDoesNotThrow(() ->
                    new Price(1L, 35455L, 1, 0,
                            BigDecimal.ZERO, "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject currency with length != 3")
        void shouldRejectCurrencyWithWrongLength() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(1L, 35455L, 1, 0,
                                    new BigDecimal("35.50"), "EU", validRange)),
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(1L, 35455L, 1, 0,
                                    new BigDecimal("35.50"), "EUROS", validRange))
            );
        }

        @Test
        @DisplayName("should reject negative priority")
        void shouldRejectNegativePriority() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Price(1L, 35455L, 1, -1,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should accept zero priority")
        void shouldAcceptZeroPriority() {
            assertDoesNotThrow(() ->
                    new Price(1L, 35455L, 1, 0,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject productId <= 0")
        void shouldRejectProductIdLessThanOrEqualToZero() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(1L, 0L, 1, 0,
                                    new BigDecimal("35.50"), "EUR", validRange)),
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(1L, -1L, 1, 0,
                                    new BigDecimal("35.50"), "EUR", validRange))
            );
        }

        @Test
        @DisplayName("should reject brandId <= 0")
        void shouldRejectBrandIdLessThanOrEqualToZero() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(0L, 35455L, 1, 0,
                                    new BigDecimal("35.50"), "EUR", validRange)),
                    () -> assertThrows(IllegalArgumentException.class, () ->
                            new Price(-1L, 35455L, 1, 0,
                                    new BigDecimal("35.50"), "EUR", validRange))
            );
        }

        @Test
        @DisplayName("should reject null brandId")
        void shouldRejectNullBrandId() {
            assertThrows(NullPointerException.class, () ->
                    new Price(null, 35455L, 1, 0,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject null productId")
        void shouldRejectNullProductId() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, null, 1, 0,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject null priceList")
        void shouldRejectNullPriceList() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, 35455L, null, 0,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject null priority")
        void shouldRejectNullPriority() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, 35455L, 1, null,
                            new BigDecimal("35.50"), "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject null price")
        void shouldRejectNullPrice() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, 35455L, 1, 0,
                            null, "EUR", validRange)
            );
        }

        @Test
        @DisplayName("should reject null currency")
        void shouldRejectNullCurrency() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, 35455L, 1, 0,
                            new BigDecimal("35.50"), null, validRange)
            );
        }

        @Test
        @DisplayName("should reject null dateRange")
        void shouldRejectNullDateRange() {
            assertThrows(NullPointerException.class, () ->
                    new Price(1L, 35455L, 1, 0,
                            new BigDecimal("35.50"), "EUR", null)
            );
        }
    }
}
