# ADR-002: Estrategia de Resolución de Prioridad de Tarifas

> **Estado:** `planning`  
> **Fecha:** 2026-07-02  
> **Owner:** Planner  
> **Decisión:** Resolución de prioridad en SQL (database-side) con `ORDER BY priority DESC LIMIT 1`

---

## 1. Contexto

El dominio de pricing requiere que, cuando múltiples tarifas son vigentes para un mismo producto/cadena/fecha, se aplique la de mayor `PRIORITY` (mayor valor numérico).

Existen dos estrategias principales para implementar esta resolución:

1. **SQL-first:** Delegar la resolución a la base de datos mediante una query que filtra por rango de fechas, ordena por prioridad descendente y limita a 1 resultado.
2. **Java-first:** Cargar todas las tarifas vigentes del rango en memoria y seleccionar la de mayor prioridad en código Java.

---

## 2. Decisión

Se adopta la estrategia **SQL-first**: la query del repository devuelve directamente la tarifa de mayor prioridad.

**Query SQL (JPQL):**
```sql
SELECT p FROM PriceEntity p
WHERE p.brandId = :brandId
  AND p.productId = :productId
  AND p.startDate <= :applicationDate
  AND p.endDate >= :applicationDate
ORDER BY p.priority DESC
```

El `Spring Data JPA` method signature será:

```java
@Query("""
    SELECT p FROM PriceEntity p
    WHERE p.brandId = :brandId
      AND p.productId = :productId
      AND p.startDate <= :applicationDate
      AND p.endDate >= :applicationDate
    ORDER BY p.priority DESC
    """)
Page<PriceEntity> findApplicablePrice(
    @Param("brandId") Long brandId,
    @Param("productId") Long productId,
    @Param("applicationDate") LocalDateTime applicationDate,
    Pageable pageable
);
```

Y en el adapter:

```java
@Override
public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
    return jpaRepository.findApplicablePrice(brandId, productId, applicationDate, PageRequest.of(0, 1))
        .getContent().stream()
        .findFirst()
        .map(mapper::toDomain);
}
```

---

## 3. Alternativas Consideradas

### 3.1 Java-first (cargar todas y filtrar en memoria)

**Ventajas:**
- La lógica de resolución es visible y testeable en el dominio.
- Fácil de extender si las reglas de desempate cambian.

**Desventajas:**
- Carga datos innecesarios en memoria (tarifas que no serán seleccionadas).
- Requiere que el dominio conozca la colección completa para tomar una decisión que la BD puede resolver eficientemente.
- Más código, más superficie de bugs.
- No justificado para un caso de uso de lectura simple sin lógica de negocio compleja en la selección.

**Conclusión:** Rechazada. La resolución por prioridad es una regla de selección simple (max), no una regla de negocio compleja. Delegarla a SQL es más eficiente y igualmente correcto.

### 3.2 SQL-first con query nativa

**Ventajas:**
- Máximo control sobre la query.

**Desventajas:**
- Pierde portabilidad entre bases de datos (aunque H2 soporta SQL estándar).
- No es necesario para este caso simple.

**Conclusión:** Rechazada. JPQL es suficiente y mantiene portabilidad.

### 3.3 SQL-first con JPQL + LIMIT (DECISIÓN ADOPTADA)

**Ventajas:**
- Eficiente: la BD filtra y ordena, solo devuelve 1 fila.
- Simple: una sola query, sin lógica adicional en Java.
- Portátil: JPQL estándar.
- Testeable: el resultado es directamente verificable con los 5 casos de prueba.
- Alineado con el principio de diseño "no sobreingeniería" (`design-patterns-standard`).

**Desventajas:**
- La lógica de selección está "oculta" en la query (no es visible como código de dominio).
- Si las reglas de desempate crecen (ej: empate de prioridad → seleccionar la de menor priceList), la query se complica.

**Mitigación:** La spec documenta la query explícitamente. Si las reglas crecen, se puede extraer a un domain service sin cambiar la arquitectura.

---

## 4. Consecuencias

### Positivas
- **Performance:** Una sola query, un solo resultado. Sin carga innecesaria.
- **Simplicidad:** Menos código, menos mappers, menos lógica en Java.
- **Correctitud:** La BD garantiza el orden y el límite.
- **Testeabilidad:** Los 5 casos de prueba validan directamente el resultado.

### Negativas
- La lógica de selección no es visible en el dominio (está en la query).
- Si las reglas de desempate crecen, la query puede volverse compleja.

### Riesgos
- **Riesgo:** Que la query no funcione correctamente con datos de prueba.  
  **Mitigación:** Los 5 casos de prueba del requisito funcional cubren todos los escenarios (una tarifa, múltiples tarifas, sin tarifas).

---

## 5. Impacto en la Arquitectura Hexagonal

- El **output port** `PriceRepositoryPort` define el método:
  ```java
  Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
  ```
- El **driven adapter** `PriceJpaAdapter` implementa este puerto delegando en `PriceJpaRepository` con la query SQL-first.
- El **domain** no conoce la estrategia de resolución; solo recibe el resultado.
- El **use case** (`GetApplicablePriceService`) llama al puerto y lanza `PriceNotFoundException` si el resultado es vacío.

---

## 6. Criterios de Revisión

Esta decisión debe revisarse si:
- Se agregan reglas de desempate adicionales (ej: empate de prioridad → menor priceList).
- Se cambia a una base de datos que no soporta `ORDER BY ... LIMIT` eficientemente.
- El volumen de tarifas por producto crece a un punto donde la query sea ineficiente (requiriendo índices adicionales).

---

## 7. Referencias

- [Context Map — Reglas de Negocio R2](../context-map.md)
- [Integration Map — Esquema de tabla PRICES](../integration-map.md)
- [Delta Spec — infrastructure.adapter.out.persistence](../../specs/increment-001-pricing-api.md)
