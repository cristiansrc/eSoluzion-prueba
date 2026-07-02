package com.esoluzion.pricing.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio que representa una tarifa de precio aplicable
 * a un producto de una cadena comercial en un rango de fechas.
 * <p>
 * Es inmutable tras su construcción. No contiene identificador de persistencia
 * (el id es un detalle de la capa de infraestructura).
 * </p>
 */
public class Price {

    private final Long brandId;
    private final Long productId;
    private final Integer priceList;
    private final Integer priority;
    private final BigDecimal price;
    private final String currency;
    private final DateRange dateRange;

    /**
     * Construye una tarifa con todas las validaciones de negocio.
     *
     * @param brandId   identificador de la cadena (no nulo, > 0)
     * @param productId identificador del producto (no nulo, > 0)
     * @param priceList identificador de la tarifa (no nulo)
     * @param priority  prioridad de la tarifa (no nulo, >= 0)
     * @param price     precio final de venta (no nulo, >= 0)
     * @param currency  código ISO 4217 (no nulo, exactamente 3 caracteres)
     * @param dateRange rango de vigencia (no nulo)
     * @throws NullPointerException     si algún argumento es nulo
     * @throws IllegalArgumentException si algún valor no cumple las restricciones
     */
    public Price(Long brandId, Long productId, Integer priceList,
                 Integer priority, BigDecimal price, String currency,
                 DateRange dateRange) {
        this.brandId = requirePositive(brandId, "brandId");
        this.productId = requirePositive(productId, "productId");
        this.priceList = Objects.requireNonNull(priceList, "priceList must not be null");
        this.priority = requireNonNegative(priority, "priority");
        this.price = requireNonNegative(price, "price");
        this.currency = validateCurrency(currency);
        this.dateRange = Objects.requireNonNull(dateRange, "dateRange must not be null");
    }

    public Long getBrandId() {
        return brandId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getPriceList() {
        return priceList;
    }

    public Integer getPriority() {
        return priority;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    // --- Métodos auxiliares de validación ---

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive, but was: " + value);
        }
        return value;
    }

    private static Integer requireNonNegative(Integer value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative, but was: " + value);
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative, but was: " + value);
        }
        return value;
    }

    private static String validateCurrency(String currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        if (currency.length() != 3) {
            throw new IllegalArgumentException(
                "currency must be a 3-character ISO 4217 code, but was: '" + currency + "'"
            );
        }
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return brandId.equals(price.brandId) &&
               productId.equals(price.productId) &&
               priceList.equals(price.priceList) &&
               priority.equals(price.priority) &&
               price.equals(price.price) &&
               currency.equals(price.currency) &&
               dateRange.equals(price.dateRange);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(brandId, productId, priceList, priority, price, currency, dateRange);
    }
}
