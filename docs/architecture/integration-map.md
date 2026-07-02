# Integration Map — eSoluzion Pricing System

> **Estado:** Accepted  
> **Última revisión:** 2026-07-02  
> **Owner:** Enterprise Architect  
> **Referencia:** [System Landscape](./system-landscape.md) | [Context Map](./context-map.md)

---

## 1. Resumen de Integraciones

| # | Integración | Tipo | Protocolo | Upstream | Downstream | Contrato |
|---|-------------|------|-----------|----------|------------|----------|
| I1 | Frontend → Backend | Sync REST | HTTP/JSON | Pricing API | Pricing Console | OpenAPI spec |
| I2 | Tests → Backend | Sync REST | HTTP/JSON | Pricing API | Integration Tests | OpenAPI spec |
| I3 | Docker Compose Networking | Internal | TCP/IP | Docker Engine | Todos los containers | docker-compose.yml |
| I4 | Backend → H2 Database | Embedded JDBC | JDBC (H2) | Pricing API | H2 (embebida) | JPA entities + Flyway migrations |

---

## 2. Detalle de Integraciones

### I1: Frontend → Backend (Sync REST)

| Atributo | Valor |
|----------|-------|
| **Tipo** | Sync API (HTTP GET) |
| **Protocolo** | HTTP/1.1 over TCP |
| **Método** | `GET` |
| **Endpoint** | `/api/prices` |
| **Query Parameters** | `applicationDate` (ISO-8601), `productId` (Long), `brandId` (Long) |
| **Response** | JSON con campos: `productId`, `brandId`, `priceList`, `startDate`, `endDate`, `price` |
| **Content-Type** | `application/json` |
| **Auth** | Ninguna (scope de prueba técnica) |
| **Timeout** | 5 segundos (defecto de Spring Boot) |
| **Retry** | No aplica (consulta idempotente, el frontend puede reintentar manualmente) |
| **Idempotencia** | Sí — GET es idempotente por definición HTTP |
| **Error Contract** | HTTP status codes estándar: 400 (bad request), 404 (no price found), 500 (internal error) |
| **Contrato canónico** | [OpenAPI spec](../../docs/api/pricing-api.yaml) |
| **Owner del contrato** | Pricing API (upstream) |
| **SLA/SLO** | No definido (prueba técnica). Esperado: < 200ms p99. |

**Ejemplo de Request:**
```http
GET /api/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
```

**Ejemplo de Response (200 OK):**
```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

**Ejemplo de Response (404 Not Found):**
```json
{
  "error": "PRICE_NOT_FOUND",
  "message": "No applicable price found for the given parameters"
}
```

---

### I2: Tests → Backend (Sync REST)

| Atributo | Valor |
|----------|-------|
| **Tipo** | Integration Tests (HTTP) |
| **Framework** | Spring Boot Test + MockMvc o TestRestTemplate |
| **Casos** | 5 casos de prueba definidos en requisitos |
| **Estrategia** | Los tests levantan el contexto Spring completo (incluyendo H2 embebida) y ejecutan requests HTTP contra el endpoint. |
| **Datos** | H2 se inicializa con migraciones Flyway antes de cada test (o se usa `@DirtiesContext` / `@Sql`) |
| **Owner** | Pricing API |
| **Quality Gate** | Cobertura mínima: 85% utilizando JaCoCo con exclusiones estándar (dto, config, entity, mapperImpl). |

**Casos de Prueba:**

| Test | applicationDate | productId | brandId | priceList esperado | price esperado |
|------|-----------------|-----------|---------|--------------------|----------------|
| T1 | 2020-06-14T10:00:00 | 35455 | 1 | 1 | 35.50 |
| T2 | 2020-06-14T16:00:00 | 35455 | 1 | 2 | 25.45 |
| T3 | 2020-06-14T21:00:00 | 35455 | 1 | 1 | 35.50 |
| T4 | 2020-06-15T10:00:00 | 35455 | 1 | 3 | 30.50 |
| T5 | 2020-06-16T21:00:00 | 35455 | 1 | 4 | 38.95 |

---

### I3: Docker Compose Networking

| Atributo | Valor |
|----------|-------|
| **Tipo** | Internal networking |
| **Driver** | bridge (default de Docker Compose) |
| **Red** | `esoluzion-network` (definida en docker-compose.yml) |
| **Servicios** | `pricing-api` (backend), `pricing-console` (frontend) |
| **DNS interno** | Los servicios se resuelven por nombre de servicio (ej: `http://pricing-api:8080`) |
| **Puertos expuestos al host** | `8080` (backend), `3000` (frontend) — configurables via `.env` |
| **Owner** | Infraestructura (docker-compose.yml) |

**Topología de red:**
```
┌─────────────────────────────────────────────┐
│         Docker Compose Network               │
│         (esoluzion-network)                  │
│                                              │
│  ┌────────────────┐   ┌────────────────┐    │
│  │  pricing-api   │   │ pricing-console│    │
│  │  :8080 (int)   │◄──│  :3000 (int)   │    │
│  │  :8080 (ext)   │   │  :3000 (ext)   │    │
│  └────────────────┘   └────────────────┘    │
│                                              │
└─────────────────────────────────────────────┘
         ▲                      ▲
         │ port mapping         │ port mapping
    Host:8080              Host:3000
```

---

### I4: Backend → H2 Database (Embedded JDBC)

| Atributo | Valor |
|----------|-------|
| **Tipo** | Embedded database (in-process) |
| **Motor** | H2 Database (mode: embedded, in-memory) |
| **URL JDBC** | `jdbc:h2:mem:pricingdb;DB_CLOSE_DELAY=-1` |
| **Inicialización** | Migraciones Flyway en classpath (`db/migration/`). Se ejecutan automáticamente al arrancar el contexto Spring. |
| **Persistencia** | Ninguna — los datos se pierden al reiniciar el backend |
| **Transaccionalidad** | Spring `@Transactional` en el adapter de repositorio |
| **Owner** | Pricing API |

**Esquema de la tabla PRICES:**

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
```

---

## 3. OpenAPI como Fuente de Verdad

El contrato de la API REST se define **primero** (API Design First) en el archivo OpenAPI:

| Atributo | Valor |
|----------|-------|
| **Ubicación** | `docs/api/pricing-api.yaml` |
| **Versión** | 1.0.0 |
| **Formato** | OpenAPI 3.0 (YAML) |
| **Consumidores** | Backend (generación de controllers), Frontend (generación de cliente o llamadas manuales), Tests (validación de contrato) |
| **Gobernanza** | Cualquier cambio al contrato debe reflejarse primero en el YAML, luego en el código. |

---

## 4. Matriz de Consistencia de Datos

| Flujo | Consistencia | Justificación |
|-------|-------------|---------------|
| Consulta de precio | **Strong** (lectura directa de H2) | Al ser una base de datos embebida en el mismo proceso, la lectura es siempre consistente. No hay replicación ni latencia. |
| Inicialización de datos | **Strong** (Flyway migrations al arrancar) | Los datos seed se cargan antes de que el backend acepte requests. |

---

## 5. Cross-Cutting Concerns

### 5.1 Seguridad

| Aspecto | Estado | Justificación |
|---------|--------|---------------|
| Autenticación | **No requerida** | Prueba técnica. En producción se usaría Keycloak + JWT. |
| Autorización | **No requerida** | Prueba técnica. |
| TLS/HTTPS | **No requerida** | Prueba técnica. En producción, TLS sería obligatorio. |
| CORS | **Configurado** | El backend permite requests desde el origen del frontend (configurable en `application.yml`). |

### 5.2 Observabilidad

| Aspecto | Estado | Justificación |
|---------|--------|---------------|
| Logs | **Básico** (Spring Boot default) | Suficiente para prueba técnica. |
| Metrics | **No requerido** | Fuera de alcance. |
| Tracing | **No requerido** | Fuera de alcance. |

### 5.3 Resiliencia

| Aspecto | Estado | Justificación |
|---------|--------|---------------|
| Timeouts | Default Spring Boot (5s) | Aceptable para prueba técnica. |
| Retries | No implementado | GET es idempotente; el frontend puede reintentar. |
| Circuit Breaker | No requerido | No hay dependencias externas. |

---

## 6. Referencias

- [System Landscape](./system-landscape.md)
- [Context Map](./context-map.md)
- [OpenAPI Spec](../../docs/api/pricing-api.yaml) (a crear por el solution architect)
- [ADR-001: Estructura del Proyecto](./decision-records/ADR-001-project-structure.md)
