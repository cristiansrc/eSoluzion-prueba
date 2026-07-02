package com.esoluzion.pricing.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResult(
    Long productId,
    Long brandId,
    Integer priceList,
    LocalDateTime startDate,
    LocalDateTime endDate,
    BigDecimal price,
    String currency
) {}
