package com.esoluzion.pricing.application.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record PriceQuery(
    LocalDateTime applicationDate,
    Long productId,
    Long brandId
) {
    public PriceQuery {
        Objects.requireNonNull(applicationDate, "applicationDate must not be null");
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(brandId, "brandId must not be null");
        if (productId <= 0) throw new IllegalArgumentException("productId must be > 0");
        if (brandId <= 0) throw new IllegalArgumentException("brandId must be > 0");
    }
}
