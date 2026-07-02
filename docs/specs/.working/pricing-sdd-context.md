# Shared Context — Increment 001: Pricing API

> **Lifecycle status:** `validated-not-executed`
> **Increment name:** pricing-api
> **Created:** 2026-07-02
> **Planner agent:** planner (qwen3.7-plus)

---

## Current status

| Field | Value |
|-------|-------|
| **Status** | `validated-not-executed` |
| **Phase** | Human-approved. All findings resolved (8 fixes applied: B1 converter, H1-H3 specs, M1-M3 docs, L1 description). |
| **Blocked by** | None |
| **Next gate** | Task Decomposer → Executor → Test Architect |

---

## Canonical artifacts

| # | Artifact | Ruta absoluta | Estado |
|---|----------|---------------|--------|
| A1 | Shared Context | `docs/specs/.working/pricing-sdd-context.md` | Activo (este archivo) |
| A2 | Delta Spec (Increment 001) | `docs/specs/increment-001-pricing-api.md` | `planning` |
| A3 | OpenAPI Contract | `docs/api/pricing-api.yaml` | `planning` |
| A4 | ADR-001: Project Structure | `docs/architecture/decision-records/ADR-001-project-structure.md` | Accepted |
| A5 | ADR-002: Priority Resolution | `docs/architecture/decision-records/ADR-002-priority-resolution-strategy.md` | `planning` |
| A6 | ADR-003: Date Type Selection | `docs/architecture/decision-records/ADR-003-date-type-selection.md` | `planning` |
| A7 | System Landscape | `docs/architecture/system-landscape.md` | Accepted |
| A8 | Context Map | `docs/architecture/context-map.md` | Accepted |
| A9 | Integration Map | `docs/architecture/integration-map.md` | Accepted |
| A10 | Workspace Mapping | `docs/architecture/workspace-mapping.md` | Accepted |
| A11 | Requirements Source | `docs/TestJava2024_1.txt` | Accepted (inmutable) |
| A12 | ADR-004: OpenAPI Generator | `docs/architecture/decision-records/ADR-004-openapi-generator.md` | Accepted |

---

## Artifact evidence

| Artifact | Evidence | Status |
|----------|----------|--------|
| A1 — Shared Context | Este archivo, creado en esta sesión | `pass` |
| A2 — Delta Spec | Creado en esta sesión en `docs/specs/increment-001-pricing-api.md` | `pass` |
| A3 — OpenAPI | Creado en esta sesión en `docs/api/pricing-api.yaml` | `pass` |
| A4 — ADR-001 | Verificado en disco, 167 líneas, estado Accepted | `pass` |
| A5 — ADR-002 | Creado en esta sesión | `pass` |
| A6 — ADR-003 | Creado en esta sesión | `pass` |
| A7 — System Landscape | Verificado en disco, 137 líneas, estado Accepted | `pass` |
| A8 — Context Map | Verificado en disco, 143 líneas, estado Accepted | `pass` |
| A9 — Integration Map | Verificado en disco, 213 líneas, estado Accepted | `pass` |
| A10 — Workspace Mapping | Verificado en disco, 145 líneas, estado Accepted | `pass` |
| A11 — Requirements | Verificado en disco, 45 líneas, datos de tabla PRICES y 5 casos de prueba | `pass` |
| A12 — ADR-004 | Creado en esta sesión, OpenAPI Generator interfaceOnly | `pass` |

---

## Decisions locked

| ID | Decisión | Justificación | ADR |
|----|----------|---------------|-----|
| D1 | Arquitectura Hexagonal con 3 capas: `domain`, `application`, `infrastructure` | Alineado con ADR-001 y skill `hexagonal-architecture`. Un solo bounded context (Pricing). | ADR-001 |
| D2 | Paquete base: `com.esoluzion.pricing` | Convención de nomenclatura definida en Workspace Mapping. | ADR-001 |
| D3 | Estrategia de resolución de prioridad: **SQL-first** (`ORDER BY priority DESC LIMIT 1`) | Evita cargar tarifas no aplicables en memoria. Más eficiente y simple. Ver ADR-002. | ADR-002 |
| D4 | Tipo de fecha: **`LocalDateTime`** (sin zona horaria) | Los datos seed no tienen zona horaria. System Landscape §6 restricción 1: "No se requiere conversión de zonas horarias." Ver ADR-003. | ADR-003 |
| D5 | OpenAPI 3.0.3 (no 3.1.x) | Compatibilidad con tooling Spring Boot estándar (springdoc-openapi). Enterprise docs referencian 3.0. | — |
| D6 | Error contract: `ApiErrorResponse` con campos `timestamp`, `status`, `error`, `code`, `message`, `path`, `trace_id`, `details` | Alineado con skill `springboot-java-rest-error-response-standards`. | — |
| D7 | Base de datos: H2 embebida en memoria, inicializada con migraciones Flyway (V1 + V2) | Requisito funcional. System Landscape §6 restricción 2. | — |
| D8 | Sin autenticación/autorización en este incremento | System Landscape §6 restricción 4. Fuera de alcance para prueba técnica. | — |
| D9 | Cobertura JaCoCo ≥ 85% con exclusiones estándar | Spring Boot stack skill + ADR-001 quality gate. | ADR-001 |
| D10 | Frontend: React 18+ con Vite, UI minimalist-ui | ADR-001 + System Landscape. Fuera del alcance de este incremento backend-first. | ADR-001 |
| D11 | OpenAPI Generator (`interfaceOnly: true`) para generar interfaces de controller y DTOs | El contrato OpenAPI es la fuente de verdad. Spring Boot genera automáticamente `PriceApi` interface y DTOs (`PriceResponse`, `ApiErrorResponse`). Config: `interfaceOnly=true`, `useSpringBoot3=true`, `useJakartaEe=true`, `dateLibrary=java8-localdatetime`. Ver ADR-004. | ADR-004 |
| D12 | MapStruct para mappers entre capas | Java stack skill. Separa claramente modelos de dominio, aplicación e infraestructura. | — |
| D13 | Moneda como `String` (ISO 4217) en dominio, no como enum cerrado | Permite extensibilidad sin cambiar dominio. Los datos seed usan EUR pero el campo es variable. | — |
| D14 | Gestión de esquema con **Flyway** (JPA ddl-auto=validate) | Flyway es el único dueño del esquema. JPA solo valida. Migraciones en `db/migration/`. | — |

---

## Spec Validator Approval

| Field | Value |
|-------|-------|
| **verdict** | `ready` |
| **reviewed_at** | 2026-07-02 |
| **validator_agent** | spec-validator (2 cycles), human-approved |
| **artifact_set_reviewed** | Full artifact set: Delta Spec, OpenAPI, Shared Context, ADR-001 through ADR-004, System Landscape, Context Map, Integration Map, Workspace Mapping |
| **summary** | All findings resolved after 3 validation cycles. Last fixes (B1, H1-H3, M1-M3, L1) applied post-revalidation: OpenAPI Generator implemented, Z handling via custom converter, Flyway migrations, JPQL with Pageable, 85% JaCoCo coverage. |
| **invalidated_by_changes_since** | none |
| **findings_report** | `docs/specs/.working/pricing-sdd-revalidation-report.md` |

---

## Human Plan Approval: approved_by_user

> ✅ **Aprobado por el usuario el 2026-07-02.** Se confirma el plan de implementación del Incremento 001: Pricing API. Stack: Java 21+, Spring Boot 3.x, Gradle, JPA, H2, Flyway, Hexagonal Architecture, OpenAPI Generator, JaCoCo ≥85%.

---

## Validator findings

> Sin hallazgos — pendiente de revisión.

---

## Resolved findings

> N/A — primera versión de la spec.

---

## Open questions

| # | Pregunta | Estado | Impacto |
|---|----------|--------|---------|
| OQ1 | ¿El frontend se incluye en este incremento o en un incremento separado? | **Decidido:** Incremento separado (002). Este incremento (001) es backend-only. | Sin impacto en 001. |
| OQ2 | ¿Se requiere Docker Compose en este incremento? | **Decidido:** Sí, infra básica (docker-compose.yml + Dockerfiles) se incluye en 001 para validación end-to-end. | Añade tareas de infra al task board. |

---

## Stale terms guard

Los siguientes términos están **prohibidos** en specs, código y OpenAPI de este incremento porque son ambiguos o pertenecen a otros contextos:

| Término prohibido | Razón | Término correcto |
|-------------------|-------|------------------|
| `Dto` (genérico) | No indica si es request, response o integration | Usar sufijos específicos: `PriceResponse`, `PriceQuery` |
| `Service` (como sufijo de dominio) | Ambiguo — ¿domain service? ¿application service? | Usar `UseCase` para application, `Resolver` para domain |
| `Manager`, `Processor`, `Handler` | No expresan responsabilidad clara | Usar nombres de negocio: `PriceResolver`, `GetApplicablePriceUseCase` |
| `PriceDto` | Ambiguo | `PriceResponse` (API), `PriceResult` (application) |
| `Tarifa` (en código) | El código usa inglés | `PriceList` (identificador), `ApplicablePrice` (resultado) |

---

## Next action

1. ✅ Planner corrections applied (all findings resolved).
2. ✅ **Human Plan Approval** obtenida.
3. ▶️ **Task Decomposer**: descomponer Incremento 001 en tareas atómicas.
4. ⬜ **Executor**: implementar backend hexagonal.
5. ⬜ **Test Architect**: implementar tests con ≥85% cobertura.
6. ⬜ **Infra + Frontend**: Docker Compose y React.

---

## Stack tecnológico detallado

| Componente | Tecnología | Versión | Justificación |
|------------|-----------|---------|---------------|
| Lenguaje | Java | 17+ (LTS) | Requisito funcional. LTS con records, pattern matching, sealed classes. |
| Framework | Spring Boot | 3.x (último estable) | Requisito funcional. Soporte para Virtual Threads, AOT, GraalVM. |
| Build | Gradle | Kotlin DSL (`.kts`) | Requisito funcional. Más conciso y type-safe que Groovy. |
| ORM | Spring Data JPA / Hibernate | 6.x (viene con Spring Boot 3.x) | Persistencia relacional con H2. |
| BD | H2 Database | Embebida (in-memory) | Requisito funcional. Efímera, sin persistencia entre reinicios. |
| Mapper | MapStruct | 1.5.x | Conversión entre capas (domain ↔ entity ↔ DTO). |
| API Contract | OpenAPI | 3.0.3 (YAML) | Design-first. Fuente de verdad del contrato REST. |
| Testing | JUnit 5 + Mockito + AssertJ | Último estable | Tests unitarios y de integración. |
| Coverage | JaCoCo | Plugin Gradle | ≥ 85% en clases testeables. |
| Frontend | React 18+ con Vite | 18.x / 5.x | UI minimalista. Fuera del alcance de incremento 001. |
| Infra | Docker Compose | 3.8+ | Orquestación backend + frontend. |
| Containerización backend | Multi-stage Dockerfile | JDK 17 + Gradle build → JRE 17 runtime | Imagen optimizada. |

---

## Estructura de paquetes hexagonal

```
com.esoluzion.pricing/
├── domain/
│   ├── model/
│   │   ├── Price.java                  # Entidad de dominio
│   │   └── DateRange.java              # Value Object (startDate, endDate)
│   ├── exception/
│   │   └── PriceNotFoundException.java # Excepción de dominio
│   └── (sin dependencias externas)
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── GetApplicablePriceUseCase.java   # Input port (interface)
│   │   └── out/
│   │       └── PriceRepositoryPort.java          # Output port (interface)
│   ├── service/
│   │   └── GetApplicablePriceService.java        # Implementación del use case
│   └── model/
│       ├── PriceQuery.java             # Query object (input del use case)
│       └── PriceResult.java            # Result object (output del use case)
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── PriceController.java          # Driving adapter (REST) — implements PriceApi (generated)
    │   │       ├── api/
    │   │       │   └── PriceApi.java             # [GENERATED by OpenAPI Generator] Interface del controller
    │   │       └── dto/
    │   │           ├── PriceResponse.java        # [GENERATED by OpenAPI Generator] API response DTO
    │   │           ├── ApiErrorResponse.java     # [GENERATED by OpenAPI Generator] Error response DTO
    │   │           └── ApiErrorDetail.java       # [GENERATED by OpenAPI Generator] Error detail DTO
    │   └── out/
    │       └── persistence/
    │           ├── PriceJpaAdapter.java           # Driven adapter (implementa PriceRepositoryPort)
    │           ├── PriceEntity.java               # JPA Entity
    │           ├── PriceJpaRepository.java        # Spring Data JPA interface
    │           └── PriceInfrastructureMapper.java               # MapStruct mapper (Entity ↔ Domain)
    ├── config/
    │   └── CorsConfig.java                       # Configuración CORS
    └── exception/
        └── GlobalExceptionHandler.java           # RestControllerAdvice (usa DTOs generados)
```

---

## Referencias a documentos enterprise

| Documento | Ruta | Relevancia para este incremento |
|-----------|------|--------------------------------|
| System Landscape | `docs/architecture/system-landscape.md` | Define containers, puertos, restricciones y flujos. |
| Context Map | `docs/architecture/context-map.md` | Lenguaje ubicuo, entidades, value objects, reglas de negocio. |
| Integration Map | `docs/architecture/integration-map.md` | Contratos de integración, esquema de tabla, casos de prueba. |
| Workspace Mapping | `docs/architecture/workspace-mapping.md` | Estructura de directorios, convenciones, dependencias entre módulos. |
| ADR-001 | `docs/architecture/decision-records/ADR-001-project-structure.md` | Monorepo, quality gate 85%. |
