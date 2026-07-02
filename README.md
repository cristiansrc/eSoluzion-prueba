# eSoluzion Pricing API

Sistema de consulta de tarifas/precios de productos para cadenas de comercio electrónico.
Prueba técnica Spring Boot con Arquitectura Hexagonal.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21+ |
| Framework | Spring Boot 3.3.+ |
| Build | Gradle (Kotlin DSL) 8.7 |
| Base de datos | H2 (en memoria) |
| Migraciones | Flyway |
| API | OpenAPI 3.0.3 (API First) |
| Generación código | OpenAPI Generator (interfaceOnly) |
| Mapeo | MapStruct |
| Frontend | React 18 + Vite |
| Tests | JUnit 5 + Mockito + AssertJ |
| Cobertura | JaCoCo (≥85%) |

---

## Frontend — Pricing Console

Interfaz web minimalista para consultar tarifas de productos.

**Funcionalidades:**
- **5 casos de prueba obligatorios**: Tabla con los tests T1–T5 del enunciado, ejecución individual o masiva con validación automática (esperado vs obtenido).
- **Consulta personalizada**: Formulario con selector de fecha, hora (HH:mm), producto y cadena. Muestra precio, tarifa, vigencia y moneda.
- **Historial de peticiones**: Log en tiempo real con método HTTP, URL y respuesta JSON de cada consulta realizada.

---

## URLs — Servicios Docker

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Frontend** | [http://localhost:3000](http://localhost:3000) | Interfaz de usuario React |
| **API REST** | [http://localhost:8080](http://localhost:8080) | Backend Spring Boot |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Documentación interactiva de la API |
| **API Docs** | [http://localhost:8080/api-docs](http://localhost:8080/api-docs) | OpenAPI spec en JSON |
| **H2 Console** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | Consola de base de datos (JDBC: `jdbc:h2:mem:pricingdb`) |

---

## Endpoint

```
GET /api/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
```

### Parámetros

| Parámetro | Tipo | Obligatorio | Descripción |
|-----------|------|-------------|-------------|
| `applicationDate` | `string` (ISO-8601) | Sí | Fecha y hora de consulta. Ej: `2020-06-14T10:00:00` o `2020-06-14T10:00:00Z` |
| `productId` | `int64` | Sí | Identificador del producto |
| `brandId` | `int64` | Sí | Identificador de la cadena (1 = ZARA) |

### Respuesta (200 OK)

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

### Códigos de error

| Status | Código | Significado |
|--------|--------|-------------|
| `400` | `VALIDATION_ERROR` | Parámetros inválidos o faltantes |
| `404` | `PRICE_NOT_FOUND` | No hay tarifa aplicable |
| `500` | `INTERNAL_ERROR` | Error inesperado del servidor |

---

## Casos de Prueba

| Test | Fecha | Hora | Producto | Cadena | Tarifa | Precio |
|------|-------|------|----------|--------|--------|--------|
| T1 | 2020-06-14 | 10:00 | 35455 | 1 (ZARA) | 1 | 35.50€ |
| T2 | 2020-06-14 | 16:00 | 35455 | 1 (ZARA) | 2 | 25.45€ |
| T3 | 2020-06-14 | 21:00 | 35455 | 1 (ZARA) | 1 | 35.50€ |
| T4 | 2020-06-15 | 10:00 | 35455 | 1 (ZARA) | 3 | 30.50€ |
| T5 | 2020-06-16 | 21:00 | 35455 | 1 (ZARA) | 4 | 38.95€ |

---

## Cómo ejecutar

### Docker Compose (recomendado)

```bash
cd infra
docker compose up --build
```

### Local — Backend

```bash
cd backend
./gradlew bootRun
```

### Local — Frontend + Backend

```bash
# Terminal 1: Backend
cd backend && ./gradlew bootRun

# Terminal 2: Frontend (con proxy a :8080)
cd frontend && npm run dev
```

### Tests

```bash
cd backend
./gradlew test
```

---

## Arquitectura del Backend

El backend sigue **Arquitectura Hexagonal** (Puertos y Adaptadores) con las siguientes capas y patrones:

### Capas

```
┌──────────────────────────────────────────────────┐
│                 Infrastructure                    │
│  (Controller, JPA, ExceptionHandler, Config)     │
├──────────────────────────────────────────────────┤
│                  Application                      │
│  (UseCase, Ports, Models)                        │
├──────────────────────────────────────────────────┤
│                    Domain                         │
│  (Entities, Value Objects, Exceptions)           │
└──────────────────────────────────────────────────┘
```

### Patrones utilizados

| Patrón | Implementación | Descripción |
|--------|---------------|-------------|
| **Value Object** | `DateRange` | Objeto inmutable con validación y comportamiento (`contains()`) |
| **Entity** | `Price` | Entidad de dominio con identidad de negocio (productId, brandId, priceList) |
| **Domain Exception** | `PriceNotFoundException` | Excepción semántica de negocio sin dependencias técnicas |
| **Input Port** | `GetApplicablePriceUseCase` | Interfaz que define qué puede hacer el sistema |
| **Output Port** | `PriceRepositoryPort` | Interfaz que define lo que el sistema necesita del exterior |
| **Use Case** | `GetApplicablePriceService` | Orquesta la lógica de aplicación, orquestando puertos de salida |
| **Repository (Data)** | `PriceJpaRepository` | Spring Data JPA con query JPQL y `Pageable` para LIMIT 1 |
| **Repository (Adapter)** | `PriceJpaAdapter` | Implementa `PriceRepositoryPort`, mapea entidades JPA a dominio |
| **Mapper** | `PriceInfrastructureMapper` | MapStruct: convierte `PriceEntity` → `Price` (dominio) |
| **Controller** | `PriceController` | Adaptador de entrada web, implementa interfaz generada por OpenAPI |
| **Exception Handler** | `GlobalExceptionHandler` | `@RestControllerAdvice` centralizado, traduce excepciones a HTTP |
| **Converter** | `StringToLocalDateTimeConverter` | Convierte strings con/sin `Z` a `LocalDateTime` |
| **OpenAPI First** | `docs/api/pricing-api.yaml` | Contrato canónico. OpenAPI Generator genera interfaces y DTOs |
| **API Contract** | `PricesApi` (generated) | Interfaz generada desde OpenAPI que el controller implementa |
| **DTO** | `PriceResponse`, `ApiErrorResponse` (generated) | Objetos de transferencia generados desde el contrato |
| **Flyway Migration** | `V1`, `V2` | Migraciones versionadas del esquema de base de datos |

### Reglas de dependencia

- **Domain** → No depende de nada (Java puro)
- **Application** → Depende solo de Domain
- **Infrastructure** → Depende de Application y Domain
- Las dependencias apuntan hacia adentro (nunca hacia afuera)

---

## Documentación del proyecto

| Recurso | Ruta |
|---------|------|
| OpenAPI canónico | `docs/api/pricing-api.yaml` |
| Shared Context SDD | `docs/specs/.working/pricing-sdd-context.md` |
| Delta Spec | `docs/specs/increment-001-pricing-api.md` |
| Task Board | `docs/specs/tasks/task-board-increment-001.md` |
| Decision Records | `docs/architecture/decision-records/ADR-00*.md` |
| System Landscape | `docs/architecture/system-landscape.md` |
| Integration Map | `docs/architecture/integration-map.md` |
