package com.esoluzion.pricing.application.port.out;

import com.esoluzion.pricing.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Puerto de salida: interfaz para la obtención de precios desde el repositorio.
 */
public interface PriceRepositoryPort {

    /**
     * Busca el precio aplicable para un producto, cadena y fecha.
     *
     * @param brandId         identificador de la cadena
     * @param productId       identificador del producto
     * @param applicationDate fecha de aplicación
     * @return Optional con el precio encontrado, o vacío si no hay tarifa aplicable
     */
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
