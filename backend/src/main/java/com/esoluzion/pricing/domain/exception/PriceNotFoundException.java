package com.esoluzion.pricing.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una tarifa aplicable
 * para los parámetros de consulta proporcionados.
 * <p>
 * Pertenece a la capa de dominio y no tiene dependencias de Spring ni HTTP.
 * El {@code GlobalExceptionHandler} la mapea a {@code 404 PRICE_NOT_FOUND}.
 * </p>
 */
public class PriceNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje fijo.
     */
    public PriceNotFoundException() {
        super("No applicable price found for the given parameters");
    }
}
