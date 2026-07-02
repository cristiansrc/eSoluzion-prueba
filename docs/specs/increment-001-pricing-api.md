# Delta Spec — Increment 001: Pricing API

> **Lifecycle status:** `planning`
> **Increment:** 001-pricing-api
> **Type:** Master Spec (greenfield — primer incremento del proyecto)
> **Created:** 2026-07-02
> **Author:** Planner
> **OpenAPI canonical:** `docs/api/pricing-api.yaml`
> **Shared context:** `docs/specs/.working/pricing-sdd-context.md`
> **Note:** D11 updated — OpenAPI Generator `interfaceOnly` para interfaces y DTOs. Ver ADR-004.

---

## 1. Objetivo del Incremento

Implementar el backend completo del sistema de consulta de tarifas de precios (Pricing API) con:
- Un endpoint REST `GET /api/prices` que resuelve la tarifa aplicable por producto, cadena y fecha.
- Arquitectura Hexagonal con 3 capas: `domain`, `application`, `infrastructure`.
- OpenAPI Generator (`interfaceOnly: true`) para generar interfaces de controller y DTOs desde el contrato OpenAPI canónico (ADR-004).
- Base de datos H2 embebida con datos seed.
- Tests de integración REST (5 casos obligatorios) + tests unitarios de dominio.
- Cobertura JaCoCo ≥ 85%.
- Infraestructura Docker Compose básica (backend + frontend placeholder).

---

## 2. Alcance

### 2.1 Incluido

| # | Elemento | Detalle |
|---|----------|---------|
| S1 | Endpoint REST | `GET /api/prices` con 3 query params. |
| S2 | Arquitectura Hexagonal | 3 capas con boundaries explícitos. |
| S3 | Dominio puro | Entidad `Price`, Value Object `DateRange`, excepción `PriceNotFoundException`. |
| S4 | Caso de uso | `GetApplicablePriceUseCase` con `PriceQuery` y `PriceResult`. |
| S5 | Persistencia JPA | `PriceEntity`, `PriceJpaRepository`, `PriceJpaAdapter`. |
| S6 | Error handling | `GlobalExceptionHandler` con `ApiErrorResponse` estándar. |
| S7 | H2 + Flyway migrations | Datos seed con 4 tarifas de ejemplo. |
| S8 | Tests de integración | 5 casos REST obligatorios + tests de error handling. |
| S9 | Tests unitarios | Tests de dominio y caso de uso con Mockito. |
| S10 | JaCoCo | Configuración con umbral 85% y exclusiones estándar. |
| S11 | Docker Compose | Backend + frontend placeholder. |
| S12 | CORS | Configuración para permitir requests desde el frontend. |

### 2.2 Excluido

| # | Elemento | Razón |
|---|----------|-------|
| X1 | Frontend React funcional | Incremento 002 separado. |
| X2 | Autenticación / Autorización | Fuera de alcance (System Landscape §6 restricción 4). |
| X3 | Observabilidad distribuida | Fuera de alcance (System Landscape §6 restricción 5). |
| X4 | CRUD de tarifas | Solo se requiere consulta. No hay creación/actualización/eliminación. |
| X5 | Paginación | Un solo resultado por consulta. |
| X6 | Conversión de monedas | Fuera de alcance. |

---

## 3. Contrato API (OpenAPI First)

### 3.1 Fuente de Verdad

| Campo | Valor |
|-------|-------|
| **Ruta canónica** | `docs/api/pricing-api.yaml` |
| **Versión OpenAPI** | 3.0.3 |
| **Versión API** | 1.0.0 |
| **Copia runtime** | `backend/src/main/resources/openapi.yaml` (copia fiel, no fuente de verdad) |

### 3.2 Endpoint Definido

| Campo | Valor |
|-------|-------|
| **Method** | `GET` |
| **Path** | `/api/prices` |
| **operationId** | `getApplicablePrice` |
| **Auth** | Ninguna (scope prueba técnica) |
| **Idempotency** | Sí — GET es idempotente por definición HTTP |
| **Side effects** | Ninguno — lectura pura |

### 3.3 Query Parameters

| Parameter | Type | Required | Format | Constraints | Example |
|-----------|------|----------|--------|-------------|---------|
| `applicationDate` | `string` | `true` | `date-time` | Pattern: `^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(Z)?$` (Z opcional para compatibilidad RFC 3339) | `2020-06-14T10:00:00Z` |
| `productId` | `integer(int64)` | `true` | — | minimum: 1 | `35455` |
| `brandId` | `integer(int64)` | `true` | — | minimum: 1 | `1` |

### 3.4 Response Schema (200 OK)

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00Z",
  "endDate": "2020-12-31T23:59:59Z",
  "price": 35.50,
  "currency": "EUR"
}
```

| Campo | Type | Required | Descripción |
|-------|------|----------|-------------|
| `productId` | `integer(int64)` | sí | Identificador del producto |
| `brandId` | `integer(int64)` | sí | Identificador de la cadena |
| `priceList` | `integer(int32)` | sí | Identificador de la tarifa |
| `startDate` | `string(date-time)` | sí | Inicio de vigencia |
| `endDate` | `string(date-time)` | sí | Fin de vigencia |
| `price` | `number(decimal)` | sí | Precio final (≥ 0) |
| `currency` | `string(3)` | sí | ISO 4217 (ej: "EUR") |

### 3.5 Error Schema (ApiErrorResponse)

```json
{
  "timestamp": "2026-07-02T18:30:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "VALIDATION_ERROR",
  "message": "The request contains invalid fields.",
  "path": "/api/prices",
  "trace_id": "4f0f6d2c6b1d4c8a",
  "details": [
    {
      "field": "applicationDate",
      "code": "REQUIRED",
      "message": "must not be null",
      "rejected_value": null
    }
  ]
}
```

### 3.6 Status Codes

| Code | Condition | Error Code |
|------|-----------|------------|
| `200` | Tarifa aplicable encontrada | — |
| `400` | Parámetros ausentes, tipo inválido o formato incorrecto | `VALIDATION_ERROR` |
| `404` | No existe tarifa aplicable para los parámetros dados | `PRICE_NOT_FOUND` |
| `500` | Error interno inesperado | `INTERNAL_ERROR` |

---

## 4. Modelo de Dominio

### 4.1 Entidades

#### `Price` (Entidad de Dominio)

| Campo | Tipo | Nullability | Descripción |
|-------|------|-------------|-------------|
| `productId` | `Long` | non-null | Identificador del producto |
| `brandId` | `Long` | non-null | Identificador de la cadena |
| `priceList` | `Integer` | non-null | Identificador de la tarifa |
| `dateRange` | `DateRange` | non-null | Rango de vigencia (Value Object) |
| `priority` | `Integer` | non-null | Prioridad de la tarifa |
| `price` | `BigDecimal` | non-null | Precio final de venta |
| `currency` | `String` | non-null | Código ISO 4217 |

**Reglas:**
- Inmutable tras construcción.
- `price` debe ser ≥ 0.
- `currency` debe tener exactamente 3 caracteres.
- `priority` debe ser ≥ 0.

### 4.2 Value Objects

#### `DateRange`

| Campo | Tipo | Nullability | Descripción |
|-------|------|-------------|-------------|
| `startDate` | `LocalDateTime` | non-null | Inicio del rango (inclusivo) |
| `endDate` | `LocalDateTime` | non-null | Fin del rango (inclusivo) |

**Reglas:**
- Inmutable.
- `startDate` debe ser ≤ `endDate`.
- Método `contains(LocalDateTime instant)`: retorna `true` si `startDate <= instant <= endDate`.

### 4.3 Excepciones de Dominio

#### `PriceNotFoundException`

| Campo | Tipo | Valor |
|-------|------|-------|
| `message` | `String` | `"No applicable price found for the given parameters"` |

- Extiende `RuntimeException`.
- No tiene dependencia de Spring ni HTTP.
- El `GlobalExceptionHandler` la mapea a `404 PRICE_NOT_FOUND`.

---

## 5. Capa de Aplicación

### 5.1 Input Port

```java
package com.esoluzion.pricing.application.port.in;

public interface GetApplicablePriceUseCase {
    PriceResult getApplicablePrice(PriceQuery query);
}
```

### 5.2 Output Port

```java
package com.esoluzion.pricing.application.port.out;

public interface PriceRepositoryPort {
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
```

**Contrato del puerto:**
- Devuelve `Optional<Price>` con la tarifa de mayor prioridad que cumple `startDate <= applicationDate <= endDate`.
- Si no hay tarifas vigentes, devuelve `Optional.empty()`.
- La resolución de prioridad se delega al adapter (SQL-first, ver ADR-002).

### 5.3 Query Object

```java
package com.esoluzion.pricing.application.model;

public record PriceQuery(
    LocalDateTime applicationDate,  // non-null
    Long productId,                 // non-null, > 0
    Long brandId                    // non-null, > 0
) {}
```

**Validaciones:**
- `applicationDate` non-null.
- `productId` non-null y > 0.
- `brandId` non-null y > 0.

### 5.4 Result Object

```java
package com.esoluzion.pricing.application.model;

public record PriceResult(
    Long productId,
    Long brandId,
    Integer priceList,
    LocalDateTime startDate,
    LocalDateTime endDate,
    BigDecimal price,
    String currency
) {}
```

### 5.5 Use Case Implementation

```java
package com.esoluzion.pricing.application.service;

@Service
public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepository;

    // Constructor injection

    @Override
    @Transactional(readOnly = true)
    public PriceResult getApplicablePrice(PriceQuery query) {
        // 1. Validar query (non-null, positive IDs)
        // 2. Llamar priceRepository.findApplicablePrice(...)
        // 3. Si empty → lanzar PriceNotFoundException
        // 4. Mapear Price → PriceResult
        // 5. Retornar PriceResult
    }
}
```

**Happy path:**
1. Validar `PriceQuery`.
2. Consultar `PriceRepositoryPort.findApplicablePrice(brandId, productId, applicationDate)`.
3. Si `Optional` está vacío → lanzar `PriceNotFoundException`.
4. Mapear `Price` (dominio) → `PriceResult` (application).
5. Retornar `PriceResult`.

**Failure paths:**
- Query inválida → `IllegalArgumentException` (capturada por handler global como `400 VALIDATION_ERROR`).
- No hay tarifa → `PriceNotFoundException` → `404 PRICE_NOT_FOUND`.
- Error inesperado → `Exception` → `500 INTERNAL_ERROR`.

---

## 6. Capa de Infraestructura

### 6.1 Driving Adapter — REST Controller

> **Nota (ADR-004):** El controller implementa la interfaz `PriceApi` generada por OpenAPI Generator. Los DTOs `PriceResponse`, `ApiErrorResponse` y `ApiErrorDetail` también son generados automáticamente en el paquete `dto`. No se implementan manualmente.

```java
package com.esoluzion.pricing.infrastructure.adapter.in.web;

import com.esoluzion.pricing.infrastructure.adapter.in.web.api.PriceApi;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.PriceResponse;
import com.esoluzion.pricing.application.model.PriceQuery;
import com.esoluzion.pricing.application.model.PriceResult;
import com.esoluzion.pricing.application.port.in.GetApplicablePriceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class PriceController implements PriceApi {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    // Constructor injection

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(
        LocalDateTime applicationDate,
        Long productId,
        Long brandId
    ) {
        PriceQuery query = new PriceQuery(applicationDate, productId, brandId);
        PriceResult result = getApplicablePriceUseCase.getApplicablePrice(query);
        PriceResponse response = new PriceResponse()
            .productId(result.productId())
            .brandId(result.brandId())
            .priceList(result.priceList())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .price(result.price())
            .currency(result.currency());
        return ResponseEntity.ok(response);
    }
}
```

**Notas sobre la interfaz generada `PriceApi`:**
- OpenAPI Generator genera `PriceApi` en el paquete `...api` con el método `getApplicablePrice` basado en el `operationId` del OpenAPI.
- Los parámetros del método reflejan los query parameters definidos en el OpenAPI.
- El tipo de retorno `ResponseEntity<PriceResponse>` se genera a partir del schema de respuesta 200.
- Las anotaciones `@GetMapping`, `@RequestParam`, `@DateTimeFormat` etc. las provee la interfaz generada; el controller solo necesita `@Override`.

#### `PriceResponse` (DTO generado por OpenAPI Generator)

> **Este DTO NO se implementa manualmente.** Se genera automáticamente en `com.esoluzion.pricing.infrastructure.adapter.in.web.dto.PriceResponse` a partir del schema `PriceResponse` del OpenAPI canónico.

| Campo | Tipo generado | JSON | Descripción |
|-------|---------------|------|-------------|
| `productId` | `Long` | `integer(int64)` | Identificador del producto |
| `brandId` | `Long` | `integer(int64)` | Identificador de la cadena |
| `priceList` | `Integer` | `integer(int32)` | Identificador de la tarifa |
| `startDate` | `LocalDateTime` | `string(date-time)` | Inicio de vigencia |
| `endDate` | `LocalDateTime` | `string(date-time)` | Fin de vigencia |
| `price` | `BigDecimal` | `number` | Precio final |
| `currency` | `String` | `string` | ISO 4217 |

**El generador incluye:** getters, setters, builder pattern (por `generateBuilders: true`), `equals()`, `hashCode()`, `toString()`, y validaciones de schema (javax.validation/jakarta.validation annotations).

### 6.2 Driven Adapter — Persistencia JPA

#### `PriceEntity` (JPA Entity)

```java
package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

@Entity
@Table(name = "prices")
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "price_list", nullable = false)
    private Integer priceList;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "curr", nullable = false, length = 3)
    private String currency;

    // Constructor protegido (JPA) + factory method
}
```

**Mapeo de columnas:**

| Columna DB | Tipo SQL | Campo Java | Tipo Java |
|------------|----------|------------|-----------|
| `id` | `BIGINT AUTO_INCREMENT PK` | `id` | `Long` |
| `brand_id` | `BIGINT NOT NULL` | `brandId` | `Long` |
| `start_date` | `TIMESTAMP NOT NULL` | `startDate` | `LocalDateTime` |
| `end_date` | `TIMESTAMP NOT NULL` | `endDate` | `LocalDateTime` |
| `price_list` | `INT NOT NULL` | `priceList` | `Integer` |
| `product_id` | `BIGINT NOT NULL` | `productId` | `Long` |
| `priority` | `INT NOT NULL` | `priority` | `Integer` |
| `price` | `DECIMAL(10,2) NOT NULL` | `price` | `BigDecimal` |
| `curr` | `VARCHAR(3) NOT NULL` | `currency` | `String` |

#### `PriceJpaRepository` (Spring Data JPA Interface)

```java
package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

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
}
```

**Nota:** Se usa `Page<PriceEntity>` con `Pageable` para garantizar `LIMIT 1`. Spring Data NO aplica LIMIT automáticamente con `Optional<T>`; sin `Pageable`, si hay empate de prioridad lanza `IncorrectResultSizeDataAccessException`. El adapter pasa `PageRequest.of(0, 1)` como Pageable.

**Índice recomendado:**
```sql
CREATE INDEX idx_prices_brand_product_dates ON prices(brand_id, product_id, start_date, end_date);
```

#### `PriceJpaAdapter` (Driven Adapter)

```java
package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.PageRequest;

@Component
public class PriceJpaAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository jpaRepository;
    private final PriceInfrastructureMapper mapper;

    // Constructor injection

    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        return jpaRepository.findApplicablePrice(brandId, productId, applicationDate, PageRequest.of(0, 1))
            .getContent().stream()
            .findFirst()
            .map(mapper::toDomain);
    }
}
```

#### `PriceInfrastructureMapper` (MapStruct)

```java
package com.esoluzion.pricing.infrastructure.adapter.out.persistence;

@Mapper(componentModel = "spring")
public interface PriceInfrastructureMapper {

    @Mapping(target = "dateRange", expression = "java(new DateRange(entity.getStartDate(), entity.getEndDate()))")
    Price toDomain(PriceEntity entity);

    // No se requiere toEntity en este incremento (solo lectura)
}
```

### 6.3 Global Exception Handler

> **Nota (ADR-004):** Los DTOs `ApiErrorResponse` y `ApiErrorDetail` son generados por OpenAPI Generator en el paquete `...dto`. El handler los importa desde allí. No se implementan manualmente.

```java
package com.esoluzion.pricing.infrastructure.exception;

import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorDetail;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. PriceNotFoundException → 404 PRICE_NOT_FOUND
    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceNotFound(
        PriceNotFoundException ex, HttpServletRequest request
    ) { ... }

    // 2. MethodArgumentNotValidException / ConstraintViolationException → 400 VALIDATION_ERROR
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(
        Exception ex, HttpServletRequest request
    ) { ... }

    // 3. HttpMessageNotReadableException → 400 INVALID_REQUEST_BODY
    //    (Defensa en profundidad — no se dispara en GET sin body, pero protege contra
    //     Content-Type incorrecto o payloads malformados si el endpoint evoluciona a POST/PUT)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
        HttpMessageNotReadableException ex, HttpServletRequest request
    ) { ... }

    // 4. TypeMismatchException (query param type error) → 400 VALIDATION_ERROR
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
        TypeMismatchException ex, HttpServletRequest request
    ) { ... }

    // 5. MissingServletRequestParameterException → 400 VALIDATION_ERROR
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(
        MissingServletRequestParameterException ex, HttpServletRequest request
    ) { ... }

    // 6. IllegalArgumentException → 400 VALIDATION_ERROR
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex, HttpServletRequest request
    ) { ... }

    // 7. Fallback: Exception → 500 INTERNAL_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
        Exception ex, HttpServletRequest request
    ) { ... }
}
```

#### `ApiErrorResponse` (DTO generado por OpenAPI Generator)

> **Este DTO NO se implementa manualmente.** Se genera automáticamente en `com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorResponse` a partir del schema `ApiErrorResponse` del OpenAPI canónico.

| Campo | Tipo generado | Descripción |
|-------|---------------|-------------|
| `timestamp` | `String` (offset-date-time) | UTC ISO-8601 |
| `status` | `Integer` | HTTP status numérico |
| `error` | `String` | Nombre estándar HTTP (ej: "BAD_REQUEST") |
| `code` | `String` | Código estable (ej: "VALIDATION_ERROR") |
| `message` | `String` | Mensaje seguro |
| `path` | `String` | Path solicitado |
| `traceId` | `String` | Correlación (UUID o thread ID). Campo Java `traceId` → serializa como `trace_id` (JSON) según convención Spring Boot (`PropertyNamingStrategies.SnakeCaseStrategy` o `@JsonProperty("trace_id")`). |
| `details` | `List<ApiErrorDetail>` | Puede estar vacío, nunca null |

#### `ApiErrorDetail` (DTO generado por OpenAPI Generator)

> **Este DTO NO se implementa manualmente.** Se genera automáticamente en `com.esoluzion.pricing.infrastructure.adapter.in.web.dto.ApiErrorDetail` a partir del schema `ApiErrorDetail` del OpenAPI canónico.

| Campo | Tipo generado | Descripción |
|-------|---------------|-------------|
| `field` | `String` | Nombre del campo con error |
| `code` | `String` | Código del error (ej: "REQUIRED") |
| `message` | `String` | Mensaje descriptivo |
| `rejectedValue` | `String` (nullable) | Valor rechazado |

**Reglas de seguridad del handler:**
- No exponer `exception.getMessage()` en el fallback 500.
- No exponer stack traces.
- No exponer clases Java, SQL ni detalles internos.
- Loguear el error real en backend con `trace_id`.
- `trace_id`: usar `UUID.randomUUID().toString()` simplificado o MDC si está disponible.

### 6.4 Converter de fechas (String → LocalDateTime)

Para soportar el sufijo `Z` (UTC) en fechas sin cambiar a `OffsetDateTime`, se registra un converter personalizado que acepta y descarta la `Z`.

```java
package com.esoluzion.pricing.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime convert(String source) {
        // Eliminar sufijo Z para usar LocalDateTime (ADR-003)
        String sanitized = source.endsWith("Z") || source.endsWith("z")
            ? source.substring(0, source.length() - 1)
            : source;
        return LocalDateTime.parse(sanitized, FORMATTER);
    }
}
```

**Nota:** Este converter se registra automáticamente por Spring Boot al ser `@Component` y `implements Converter<String, LocalDateTime>`. También se requiere un `@JsonComponent` similar para la deserialización JSON de query params si se usa `@RequestParam` con `LocalDateTime` (Spring Boot 3.x lo maneja automáticamente con `@DateTimeFormat` y el converter de String).

---

## 7. Modelo de Datos

### 7.1 Esquema de Tabla

```sql
CREATE TABLE prices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT        NOT NULL,
    start_date  TIMESTAMP     NOT NULL,
    end_date    TIMESTAMP     NOT NULL,
    price_list  INT           NOT NULL,
    product_id  BIGINT        NOT NULL,
    priority    INT           NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    curr        VARCHAR(3)    NOT NULL
);

CREATE INDEX idx_prices_brand_product_dates
    ON prices(brand_id, product_id, start_date, end_date);
```

### 7.2 Migraciones Flyway

Se utiliza **Flyway** para la gestión del esquema y datos iniciales. JPA opera con `ddl-auto: validate` para verificar que las entidades coinciden con el esquema gestionado por Flyway.

**Ubicación:** `src/main/resources/db/migration/`

**Migración V1 — Creación de la tabla:**
```sql
-- V1__create_prices_table.sql
CREATE TABLE prices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT       NOT NULL,
    start_date  TIMESTAMP    NOT NULL,
    end_date    TIMESTAMP    NOT NULL,
    price_list  INT          NOT NULL,
    product_id  BIGINT       NOT NULL,
    priority    INT          NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    curr        VARCHAR(3)   NOT NULL
);

CREATE INDEX idx_prices_brand_product_dates 
    ON prices(brand_id, product_id, start_date, end_date);
```

**Migración V2 — Datos semilla:**
```sql
-- V2__seed_prices_data.sql
INSERT INTO prices (brand_id, start_date, end_date, price_list, product_id, priority, price, curr) VALUES
(1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 1, 35455, 0, 35.50, 'EUR'),
(1, '2020-06-14 15:00:00', '2020-06-14 18:30:00', 2, 35455, 1, 25.45, 'EUR'),
(1, '2020-06-15 00:00:00', '2020-06-15 11:00:00', 3, 35455, 1, 30.50, 'EUR'),
(1, '2020-06-15 16:00:00', '2020-12-31 23:59:59', 4, 35455, 1, 38.95, 'EUR');
```

### 7.3 Retención y Consistencia

| Aspecto | Política |
|---------|----------|
| Persistencia | Efímera (H2 in-memory). Se reinicia con cada arranque. |
| Retención | No aplica — datos seed estáticos. |
| Consistencia | Strong (lectura directa de BD embebida). |
| Backup | No requerido (prueba técnica). |
| Soft delete | No aplica — tabla de solo lectura. |
| Auditoría | No aplica — datos seed, no hay escritura runtime. |

---

## 8. Configuración

### 8.1 `application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: pricing-api
  datasource:
    url: jdbc:h2:mem:pricingdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
    database-platform: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.esoluzion.pricing: INFO
    org.hibernate.SQL: WARN
```

**Notas:**
- `ddl-auto: validate` — JPA solo valida que las entidades coinciden con el esquema gestionado por Flyway.
- Flyway gestiona tanto el DDL (V1) como los datos seed (V2).
- `baseline-on-migrate: true` — permite que Flyway se basele sobre una BD vacía en el primer arranque.

### 8.2 Esquema Flyway

Flyway gestiona el esquema mediante migraciones ubicadas en `src/main/resources/db/migration/`. No se usan `schema.sql` ni `data.sql`.

| Archivo | Propósito |
|---------|-----------|
| `V1__create_prices_table.sql` | Creación de la tabla `prices` + índices |
| `V2__seed_prices_data.sql` | Datos semilla (4 registros de tarifas) |

Ver sección §7.2 para el contenido completo de las migraciones.

### 8.3 CORS Configuration

```java
package com.esoluzion.pricing.infrastructure.config;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET")
            .allowedHeaders("*");
    }
}
```

---

## 9. Build — Gradle (Kotlin DSL)

### 9.1 `build.gradle.kts` (dependencias clave)

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.3.+"
    id("io.spring.dependency-management") version "1.1.+"
    id("org.openapi.generator") version "7.6.0"
    jacoco
}

group = "com.esoluzion"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Flyway
    implementation("org.flywaydb:flyway-core")

    // H2
    runtimeOnly("com.h2database:h2")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ──────────────────────────────────────────────
// OpenAPI Generator — interfaceOnly (ADR-004)
// ──────────────────────────────────────────────
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${rootDir}/../docs/api/pricing-api.yaml")
    apiPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web.api")
    modelPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web.dto")
    invokerPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web")
    outputDir.set("${projectDir}/build/generated")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "dateLibrary" to "java8-localdatetime",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "generateBuilders" to "true",
        "library" to "spring-boot"
    ))
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

sourceSets {
    main {
        java {
            srcDir("${projectDir}/build/generated/src/main/java")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

// JaCoCo
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/dto/**",
                    "**/entity/**",
                    "**/config/**",
                    "**/exceptions/**",
                    "**/exception/**",
                    "**/*MapperImpl*",
                    "**/*Application*"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
```

### 9.2 Exclusiones JaCoCo

| Patrón | Razón |
|--------|-------|
| `**/dto/**` | DTOs de transporte (data carriers sin lógica) |
| `**/entity/**` | Entidades JPA (modelo de persistencia, sin lógica de negocio) |
| `**/config/**` | Clases de configuración Spring (wiring) |
| `**/exception/**` y `**/exceptions/**` | DTOs de error y excepciones (data carriers) |
| `**/*MapperImpl*` | Implementaciones generadas por MapStruct |
| `**/*Application*` | Clase principal de Spring Boot (entry point) |

---

## 10. Estrategia de Tests

### 10.1 Tests de Integración REST (obligatorios)

| Test | applicationDate | productId | brandId | priceList esperado | price esperado | HTTP Status |
|------|-----------------|-----------|---------|--------------------|----------------|-------------|
| T1 | `2020-06-14T10:00:00Z` | 35455 | 1 | 1 | 35.50 | 200 |
| T2 | `2020-06-14T16:00:00Z` | 35455 | 1 | 2 | 25.45 | 200 |
| T3 | `2020-06-14T21:00:00Z` | 35455 | 1 | 1 | 35.50 | 200 |
| T4 | `2020-06-15T10:00:00Z` | 35455 | 1 | 3 | 30.50 | 200 |
| T5 | `2020-06-16T21:00:00Z` | 35455 | 1 | 4 | 38.95 | 200 |

**Framework:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` o `MockMvc`.

### 10.2 Tests de Error Handling

| Test | Escenario | Status esperado | Code esperado |
|------|-----------|-----------------|---------------|
| E1 | `applicationDate` ausente | 400 | `VALIDATION_ERROR` |
| E2 | `productId` con valor no numérico | 400 | `VALIDATION_ERROR` |
| E3 | Producto inexistente (productId=99999) | 404 | `PRICE_NOT_FOUND` |
| E4 | Fecha sin tarifa (2019-01-01T00:00:00Z) | 404 | `PRICE_NOT_FOUND` |
| E5 | Formato de fecha inválido | 400 | `VALIDATION_ERROR` |

### 10.3 Tests Unitarios de Dominio

| Test | Descripción |
|------|-------------|
| U1 | `DateRange.contains()` — fecha dentro del rango retorna true |
| U2 | `DateRange.contains()` — fecha fuera del rango retorna false |
| U3 | `DateRange.contains()` — fecha en el límite exacto (startDate) retorna true |
| U4 | `DateRange.contains()` — fecha en el límite exacto (endDate) retorna true |
| U5 | `DateRange` constructor — startDate > endDate lanza excepción |

### 10.4 Tests Unitarios del Use Case

| Test | Descripción |
|------|-------------|
| UC1 | `getApplicablePrice` — repository retorna Price → retorna PriceResult |
| UC2 | `getApplicablePrice` — repository retorna empty → lanza PriceNotFoundException |
| UC3 | `getApplicablePrice` — query con productId null → lanza IllegalArgumentException |

### 10.5 Herramientas y Cobertura

| Herramienta | Propósito |
|-------------|-----------|
| JUnit 5 | Framework de tests |
| Mockito | Mocks para unit tests (PriceRepositoryPort) |
| AssertJ | Assertions fluidas |
| Spring Boot Test | Contexto completo para integration tests |
| JaCoCo | Cobertura ≥ 85% (con exclusiones) |

---

## 11. Infraestructura Docker

### 11.1 `docker-compose.yml`

```yaml
services:
  pricing-api:
    build:
      context: ..
      dockerfile: infra/Dockerfile.backend
    ports:
      - "${BACKEND_PORT:-8080}:8080"
    networks:
      - esoluzion-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/prices?applicationDate=2020-06-14T10:00:00Z&productId=35455&brandId=1"]
      interval: 30s
      timeout: 10s
      retries: 3

  pricing-console:
    build:
      context: ../frontend
      dockerfile: ../infra/Dockerfile.frontend
    ports:
      - "${FRONTEND_PORT:-3000}:80"
    networks:
      - esoluzion-network
    depends_on:
      - pricing-api

networks:
  esoluzion-network:
    driver: bridge
```

### 11.2 `Dockerfile.backend` (multi-stage)

```dockerfile
# Stage 1: Build
FROM gradle:8-jdk17 AS build
WORKDIR /app
# Copiar archivos de build
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
# Copiar código fuente
COPY src ./src
# Copiar OpenAPI spec para generación de código (OpenAPI Generator)
COPY ../docs ./docs
RUN gradle build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Nota sobre el build context:** El `context` de docker-compose debe ser el directorio raíz del proyecto (no `backend/`) para que el `COPY ../docs ./docs` funcione. Si el context es `../backend`, la ruta `../docs` queda fuera del contexto Docker y el build fallará. Ajustar el `context` a `..` y el `dockerfile` a `infra/Dockerfile.backend` en `docker-compose.yml`.

---

## 12. Criterios de Aceptación

### 12.1 Funcionales

| # | Criterio | Verificación |
|---|----------|-------------|
| AC1 | El endpoint `GET /api/prices` retorna 200 con los datos correctos para los 5 casos de prueba | Tests T1-T5 pasan |
| AC2 | El endpoint retorna 404 con `ApiErrorResponse` cuando no hay tarifa aplicable | Tests E3, E4 pasan |
| AC3 | El endpoint retorna 400 con `ApiErrorResponse` cuando los parámetros son inválidos | Tests E1, E2, E5 pasan |
| AC4 | La respuesta 200 incluye todos los campos: productId, brandId, priceList, startDate, endDate, price, currency | Verificación de schema |
| AC5 | La resolución de prioridad funciona correctamente (mayor priority gana) | Test T2 (priority 1 > priority 0) |

### 12.2 Técnicos

| # | Criterio | Verificación |
|---|----------|-------------|
| AC6 | El dominio no depende de Spring, JPA, HTTP ni ningún framework | Revisión de imports en `domain/` |
| AC7 | La aplicación no depende de infraestructura | Revisión de imports en `application/` |
| AC8 | Las entidades JPA no se exponen fuera de infraestructura | Revisión de imports |
| AC9 | El contrato OpenAPI coincide con la implementación runtime | Comparación manual |
| AC10 | La cobertura JaCoCo es ≥ 85% (con exclusiones) | `gradle jacocoTestCoverageVerification` |
| AC11 | Los 5 casos de prueba obligatorios pasan | `gradle test` |
| AC12 | El `ApiErrorResponse` tiene todos los campos obligatorios | Tests de error handling |
| AC13 | No hay respuestas HTML ni Whitelabel Error Page | Tests de error handling |
| AC14 | Docker Compose levanta backend y frontend correctamente | `docker-compose up` |

---

## 13. Decomposition Contract

Para el Task Decomposer, los archivos autoritativos y el orden permitido:

### 13.1 Archivos Autoritativos

| Archivo | Ruta |
|---------|------|
| Delta Spec | `docs/specs/increment-001-pricing-api.md` |
| Shared Context | `docs/specs/.working/pricing-sdd-context.md` |
| OpenAPI | `docs/api/pricing-api.yaml` |
| ADR-002 | `docs/architecture/decision-records/ADR-002-priority-resolution-strategy.md` |
| ADR-003 | `docs/architecture/decision-records/ADR-003-date-type-selection.md` |
| ADR-004 | `docs/architecture/decision-records/ADR-004-openapi-generator.md` |

### 13.2 Canonical Paths y Names

| Elemento | Nombre canónico |
|----------|-----------------|
| Endpoint | `GET /api/prices` |
| operationId | `getApplicablePrice` |
| API Interface (generated) | `PriceApi` (paquete `...web.api`) |
| Response DTO (generated) | `PriceResponse` (paquete `...web.dto`) |
| Error DTO (generated) | `ApiErrorResponse` (paquete `...web.dto`) |
| Error Detail DTO (generated) | `ApiErrorDetail` (paquete `...web.dto`) |
| Query | `PriceQuery` |
| Result | `PriceResult` |
| Domain Entity | `Price` |
| Value Object | `DateRange` |
| Domain Exception | `PriceNotFoundException` |
| Input Port | `GetApplicablePriceUseCase` |
| Output Port | `PriceRepositoryPort` |
| Use Case Impl | `GetApplicablePriceService` |
| JPA Entity | `PriceEntity` |
| JPA Repository | `PriceJpaRepository` |
| Driven Adapter | `PriceJpaAdapter` |
| Mapper | `PriceInfrastructureMapper` |
| Controller | `PriceController` (implements `PriceApi`) |
| Exception Handler | `GlobalExceptionHandler` |
| DB Table | `prices` |
| DB Columns | `id`, `brand_id`, `start_date`, `end_date`, `price_list`, `product_id`, `priority`, `price`, `curr` |

### 13.3 Orden de Implementación Sugerido

1. **Configuración del proyecto:** Gradle, estructura de paquetes, application.yml.
2. **OpenAPI Generator (ADR-004):** Configurar plugin `org.openapi.generator` en `build.gradle.kts`. Verificar que `gradle openApiGenerate` genera correctamente `PriceApi`, `PriceResponse`, `ApiErrorResponse`, `ApiErrorDetail` en `build/generated/`. Verificar que `compileJava` depende de `openApiGenerate`.
3. **Migraciones Flyway:** `V1__create_prices_table.sql`, `V2__seed_prices_data.sql`.
4. **Dominio:** `Price`, `DateRange`, `PriceNotFoundException`.
5. **Aplicación:** `PriceQuery`, `PriceResult`, `GetApplicablePriceUseCase`, `PriceRepositoryPort`, `GetApplicablePriceService`.
6. **Infraestructura — Persistencia:** `PriceEntity`, `PriceJpaRepository`, `PriceInfrastructureMapper`, `PriceJpaAdapter`.
7. **Infraestructura — Web:** `PriceController` (implementa `PriceApi` generada). Mapeo de `PriceResult` → `PriceResponse` (DTO generado).
8. **Infraestructura — Errores:** `GlobalExceptionHandler` (usa `ApiErrorResponse` y `ApiErrorDetail` generados).
9. **Tests unitarios:** Dominio + Use Case.
10. **Tests de integración:** REST (5 casos + error handling).
11. **JaCoCo:** Configuración y verificación de cobertura.
12. **Infra:** Docker Compose, Dockerfiles.
13. **CORS:** `CorsConfig`.

### 13.4 Forbidden Stale Terms

No usar en código, specs ni commits:
- `PriceDto` → usar `PriceResponse` (API) o `PriceResult` (application)
- `PriceService` (como nombre de dominio) → usar `PriceResolver` o `GetApplicablePriceService`
- `Tarifa` (en código) → usar `PriceList` (ID) o `ApplicablePrice`
- `Manager`, `Processor`, `Handler` (como sufijo de dominio) → usar nombres de negocio

---

## 14. Riesgos y Mitigaciones

| Riesgo | Impacto | Probabilidad | Mitigación |
|--------|---------|-------------|------------|
| Flyway no ejecuta migraciones en orden alfabético | Alto | Baja | Usar convención `V{num}__{desc}.sql`. Flyway ordena por versión numérica de forma fiable. |
| JPQL `ORDER BY ... DESC` con `Pageable` puede no limitar correctamente si se omite Pageable | Medio | Baja | El adapter siempre pasa `PageRequest.of(0, 1)`. Verificar con tests de integración. |
| MapStruct no genera el mapper | Alto | Baja | Configurar annotation processor correctamente en Gradle |
| JaCoCo no alcanza 85% | Medio | Media | Excluir correctamente las clases no testeables |
| Formato de fecha en OpenAPI no coincide con Spring deserialization | Medio | Media | Usar `@DateTimeFormat(iso = ISO.DATE_TIME)` + pattern en OpenAPI |

---

## 15. Referencias

| Documento | Ruta |
|-----------|------|
| Shared Context | `docs/specs/.working/pricing-sdd-context.md` |
| OpenAPI Contract | `docs/api/pricing-api.yaml` |
| ADR-001: Project Structure | `docs/architecture/decision-records/ADR-001-project-structure.md` |
| ADR-002: Priority Resolution | `docs/architecture/decision-records/ADR-002-priority-resolution-strategy.md` |
| ADR-003: Date Type Selection | `docs/architecture/decision-records/ADR-003-date-type-selection.md` |
| ADR-004: OpenAPI Generator | `docs/architecture/decision-records/ADR-004-openapi-generator.md` |
| System Landscape | `docs/architecture/system-landscape.md` |
| Context Map | `docs/architecture/context-map.md` |
| Integration Map | `docs/architecture/integration-map.md` |
| Requirements Source | `docs/TestJava2024_1.txt` |
