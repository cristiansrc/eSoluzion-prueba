# ADR-003: Selección del Tipo de Dato para Fechas

> **Estado:** `planning`  
> **Fecha:** 2026-07-02  
> **Owner:** Planner  
> **Decisión:** Usar `LocalDateTime` (sin zona horaria) en todo el stack

---

## 1. Contexto

El sistema maneja fechas y horas para:
- **Input:** `applicationDate` (fecha de consulta del precio).
- **Dominio:** `startDate` y `endDate` (rango de vigencia de una tarifa).
- **Output:** `startDate` y `endDate` en la respuesta API.
- **Persistencia:** Columnas `START_DATE` y `END_DATE` en la tabla PRICES.
- **Seed data:** Los datos de ejemplo usan formato `2020-06-14-00.00.00` sin zona horaria.

Opciones consideradas:
1. `LocalDateTime` — fecha y hora sin zona horaria.
2. `OffsetDateTime` — fecha y hora con offset UTC.
3. `ZonedDateTime` — fecha y hora con zona horaria completa (zona + offset).
4. `Instant` — instante absoluto en UTC.

---

## 2. Decisión

Se adopta **`LocalDateTime`** en todas las capas:
- **API (OpenAPI):** `type: string, format: date-time` con pattern `yyyy-MM-dd'T'HH:mm:ss` (Z opcional para compatibilidad RFC 3339).
- **Dominio:** `LocalDateTime` en `DateRange` y `Price`.
- **Aplicación:** `LocalDateTime` en `PriceQuery` y `PriceResult`.
- **Infraestructura (JPA):** `LocalDateTime` mapeado a `TIMESTAMP` en H2.
- **Seed data:** `V2__seed_prices_data.sql` (migración Flyway) usa formato `2020-06-14 00:00:00` (sin offset).

---

## 3. Alternativas Consideradas

### 3.1 OffsetDateTime (con offset UTC)

**Ventajas:**
- Representa un instante absoluto sin ambigüedad.
- Mejor práctica para sistemas distribuidos o multi-zona horaria.
- Spring Boot / Jackson lo serializan con offset (ej: `2020-06-14T10:00:00Z`).

**Desventajas:**
- Los datos seed no tienen zona horaria ni offset.
- Requiere conversión explícita entre el input del usuario (sin offset) y el almacenamiento (con offset).
- Añade complejidad innecesaria para un sistema que opera en una sola zona horaria.
- System Landscape §6 restricción 1: "No se requiere conversión de zonas horarias."

**Conclusión:** Rechazada. La complejidad no está justificada para este alcance.

### 3.2 ZonedDateTime (con zona horaria)

**Ventajas:**
- Máxima precisión temporal.
- Soporta reglas de horario de verano (DST).

**Desventajas:**
- Overkill para un sistema sin requisitos de zona horaria.
- Los datos seed no especifican zona.
- Serialización más compleja (incluye zona: `2020-06-14T10:00:00+02:00[Europe/Madrid]`).
- No alineado con la simplicidad requerida.

**Conclusión:** Rechazada. No hay requisito de zona horaria.

### 3.3 Instant (instante UTC)

**Ventajas:**
- Representa un punto exacto en la línea de tiempo.
- Ideal para auditoría y logging.

**Desventajas:**
- Los datos seed no están en UTC.
- Requiere conversión desde LocalDateTime del input.
- No aporta valor para comparaciones de rango cuando todos los datos están en la misma zona.

**Conclusión:** Rechazada. No hay beneficio sobre LocalDateTime para este caso.

### 3.4 LocalDateTime (DECISIÓN ADOPTADA)

**Ventajas:**
- **Alineado con los datos seed:** Los datos de ejemplo no tienen zona horaria.
- **Simple:** Sin conversiones, sin offsets, sin zonas.
- **Directo:** El input del usuario (`2020-06-14T10:00:00`) se usa tal cual.
- **Eficiente:** Mapeo directo a `TIMESTAMP` en H2.
- **Suficiente:** System Landscape confirma que no se requiere conversión de zonas.
- **Consistente:** El mismo tipo en API, dominio, aplicación y persistencia.

**Desventajas:**
- No representa un instante absoluto (ambiguo si el sistema se distribuye a múltiples zonas).
- Si en el futuro se requiere multi-zona, habría que migrar.

**Mitigación:** Si el sistema crece a multi-zona, la migración de `LocalDateTime` a `OffsetDateTime` es mecánica y está bien soportada por JPA/Hibernate.

---

## 4. Consecuencias

### Positivas
- **Simplicidad:** Un solo tipo de fecha en todo el stack.
- **Consistencia:** Sin conversiones entre capas.
- **Alineado con datos seed:** Los datos de ejemplo funcionan sin transformación.
- **Performance:** Sin overhead de conversión de zonas.
- **Testeabilidad:** Los casos de prueba usan el mismo formato que el input.

### Negativas
- No soporta multi-zona horaria (fuera de alcance actual).
- Si se expusiera a múltiples zonas, requeriría migración.

### Riesgos
- **Riesgo:** Que un cliente envíe una fecha con offset (ej: `2020-06-14T10:00:00Z`).  
  **Mitigación:** El OpenAPI define un pattern que acepta `Z` opcional (yyyy-MM-dd'T'HH:mm:ss(Z)?) para compatibilidad con RFC 3339. Se documenta un Converter personalizado que descarta la `Z` al deserializar. El `GlobalExceptionHandler` retorna 400 si el formato es inválido.

---

## 5. Impacto en las Capas

| Capa | Tipo | Mapeo |
|------|------|-------|
| OpenAPI | `string` + `format: date-time` + `pattern` | Serialización: `2020-06-14T10:00:00` |
| Controller (DTO) | `LocalDateTime` | Jackson: `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` |
| Application (Query/Result) | `LocalDateTime` | Sin conversión |
| Domain (DateRange, Price) | `LocalDateTime` | Value Object inmutable |
| JPA Entity | `LocalDateTime` | `@Column(columnDefinition = "TIMESTAMP")` |
| H2 Table | `TIMESTAMP` | `START_DATE TIMESTAMP NOT NULL` |
| V2__seed_prices_data.sql (Flyway) | Literal SQL | `'2020-06-14 00:00:00'` |

---

## 6. Criterios de Revisión

Esta decisión debe revisarse si:
- Se requiere soporte para múltiples zonas horarias.
- Se integra con sistemas externos que usan `OffsetDateTime` o `Instant`.
- Se requiere auditoría de instantes absolutos (ej: `created_at` en UTC).

---

## 7. Referencias

- [System Landscape §6 — Restricción 1](../system-landscape.md)
- [Context Map — Entidades del Dominio](../context-map.md)
- [Integration Map — Esquema de tabla PRICES](../integration-map.md)
- [OpenAPI Contract — ApplicationDateParam](../../api/pricing-api.yaml)
