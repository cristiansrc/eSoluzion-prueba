package com.esoluzion.pricing.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object inmutable que representa un rango de fechas con inicio y fin.
 * <p>
 * Los extremos startDate y endDate son inclusivos.
 * </p>
 */
public class DateRange {

    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    /**
     * Construye un DateRange validando que startDate no sea posterior a endDate.
     *
     * @param startDate inicio del rango (no nulo)
     * @param endDate   fin del rango (no nulo)
     * @throws NullPointerException     si algún argumento es nulo
     * @throws IllegalArgumentException si startDate es posterior a endDate
     */
    public DateRange(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    /**
     * Verifica si una fecha está dentro del rango [startDate, endDate] (inclusivo).
     *
     * @param dateTime la fecha a verificar
     * @return {@code true} si {@code startDate <= dateTime <= endDate}
     * @throws NullPointerException si dateTime es nulo
     */
    public boolean contains(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return !dateTime.isBefore(startDate) && !dateTime.isAfter(endDate);
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return startDate.equals(dateRange.startDate) && endDate.equals(dateRange.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "DateRange{startDate=" + startDate + ", endDate=" + endDate + "}";
    }
}
