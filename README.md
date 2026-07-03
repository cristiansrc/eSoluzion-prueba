# Inditex Pricing API

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

### Cobertura JaCoCo

```bash
cd backend
./gradlew jacocoTestCoverageVerification
```

El reporte HTML de cobertura se genera en:
```
backend/build/reports/jacoco/test/html/index.html
```

**Cobertura actual:** 100% (instrucciones) en clases testeables.

> Exclusiones JaCoCo: DTOs generados, entidades JPA, configuración, excepciones,
> mappers MapStruct, clases de aplicación Spring Boot, interfaces generadas,
> handlers de error y clases de ayuda.

---

## Tests del Frontend

El frontend no incluye tests automatizados en este incremento.
Para verificarlo, ejecutar el build:

```bash
cd frontend
npm run build
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

| Recurso | Ruta | Descripción |
|---------|------|-------------|
| OpenAPI canónico | `docs/api/pricing-api.yaml` | Contrato OpenAPI 3.0.3. Fuente de verdad del API. Define endpoint `/api/prices`, schemas `PriceResponse`, `ApiErrorResponse` y códigos de error. |
| Shared Context SDD | `docs/specs/.working/pricing-sdd-context.md` | Contexto compartido del incremento. Contiene estado actual, artefactos canónicos, decisiones bloqueadas (D1–D14) y aprobaciones. |
| Delta Spec | `docs/specs/increment-001-pricing-api.md` | Especificación detallada del Incremento 001. Incluye modelo de dominio, puertos, casos de uso, adaptadores, configuración, tests y criterios de aceptación. |
| Task Board | `docs/specs/tasks/task-board-increment-001.md` | Descomposición en 16 tareas atómicas con dependencias, archivos, criterios de aceptación y agente asignado. |
| ADR-001 | `docs/architecture/decision-records/ADR-001-project-structure.md` | Decisión de estructura monorepo con módulos backend/, frontend/, infra/ y docs/. |
| ADR-002 | `docs/architecture/decision-records/ADR-002-priority-resolution-strategy.md` | Estrategia SQL-first para resolución de prioridad de tarifas con `ORDER BY priority DESC` y `Pageable` para LIMIT 1. |
| ADR-003 | `docs/architecture/decision-records/ADR-003-date-type-selection.md` | Selección de `LocalDateTime` sin zona horaria, con `Z` opcional para compatibilidad RFC 3339 y converter personalizado. |
| ADR-004 | `docs/architecture/decision-records/ADR-004-openapi-generator.md` | Generación de interfaces REST y DTOs mediante OpenAPI Generator con `interfaceOnly: true`. |
| System Landscape | `docs/architecture/system-landscape.md` | Modelo C4 Level 1 y 2. Define actores (Desarrollador/QA), sistemas (Pricing API, Pricing Console, H2), containers y restricciones. |
| Context Map | `docs/architecture/context-map.md` | Bounded context "Pricing" con lenguaje ubicuo (Price, Brand, Product, PriceList, Priority). Fuente de verdad y entidades de dominio. |
| Integration Map | `docs/architecture/integration-map.md` | Contratos de integración: Frontend→Backend (REST), Tests→Backend, Docker networking y Backend→H2. Incluye esquema SQL y 5 casos de prueba. |
| Workspace Mapping | `docs/architecture/workspace-mapping.md` | Mapeo del workspace monorepo. Estructura de directorios, convenciones de nomenclatura, dependencias entre módulos y configuración Docker. |
