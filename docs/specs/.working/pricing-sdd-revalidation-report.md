# Spec Validator Re-Validation Report — Increment 001: Pricing API

> **Validator:** spec-validator
> **Date:** 2026-07-02
> **Re-validation after:** Planner corrections (JPQL blocker, Flyway migration, OpenAPI format, Integration Map)
> **Verdict:** `ready with minor changes`

---

## 1. Double-Check Evidence Pass

### Bloque 1: Correcciones del blocker JPQL

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| Repositorio JPA usa `Page<PriceEntity>` con `Pageable` | ✅ PASS | Delta Spec L397-417: `Page<PriceEntity> findApplicablePrice(... Pageable pageable)` |
| Adapter pasa `PageRequest.of(0, 1)` | ✅ PASS | Delta Spec L444: `jpaRepository.findApplicablePrice(brandId, productId, applicationDate, PageRequest.of(0, 1))` |
| Nota aclara LIMIT 1 no automático con Optional | ✅ PASS | Delta Spec L420: "Spring Data NO aplica LIMIT automáticamente con `Optional<T>`" |
| ADR-002 refleja la solución correcta | ❌ FAIL | ADR-002 L36-43 aún muestra `Optional<PriceEntity> findTopByBrandIdAndProductIdAnd...` como método primario. Solo menciona `@Query` + `Pageable` como alternativa ("O equivalentemente..."). |

### Bloque 2: Migración a Flyway

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| `application.yml` usa `ddl-auto: validate` | ✅ PASS | Delta Spec L645: `ddl-auto: validate` |
| `application.yml` tiene `spring.flyway.enabled: true` | ✅ PASS | Delta Spec L651-654: `flyway: enabled: true, locations: classpath:db/migration` |
| No hay referencias a `schema.sql`/`data.sql` como mecanismo | ❌ FAIL | **6 referencias residuales** (ver Findings F1-F5, F7) |
| Migraciones Flyway definidas (V1 DDL, V2 seed) | ✅ PASS | Delta Spec L586-612: V1__create_prices_table.sql + V2__seed_prices_data.sql |
| Gradle incluye `flyway-core` | ✅ PASS | Delta Spec L734: `implementation("org.flywaydb:flyway-core")` |
| Orden de implementación: Flyway antes que persistencia | ✅ PASS | Delta Spec L984: paso 2 = Flyway, paso 5 = persistencia |
| Shared Context menciona decisión Flyway | ✅ PASS (parcial) | D14 (L74) correcta, pero D7 (L67) aún dice `data.sql` — contradicción interna |
| Workspace Mapping refleja `db/migration/` | ✅ PASS | Workspace Mapping L45-48: estructura con `db/migration/` + V1 + V2 |
| System Landscape no menciona schema.sql/data.sql | ❌ FAIL | System Landscape L68 y L112 aún referencian `data.sql` |

### Bloque 3: OpenAPI

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| No existe `format: decimal` en el archivo | ✅ PASS | `grep "format:\s*decimal"` en pricing-api.yaml → 0 resultados |
| Campo `price` usa `type: number` sin format inválido | ✅ PASS | OpenAPI L130-134: `type: number`, `minimum: 0`, sin `format` |

### Bloque 4: Integration Map

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| Ejemplo response 200 OK incluye `currency: "EUR"` | ✅ PASS | Integration Map L57: `"currency": "EUR"` |
| No hay referencias a `data.sql` | ✅ PASS | `grep "data\.sql"` en integration-map.md → 0 resultados |
| Referencias de inicialización mencionan Flyway | ✅ PASS | Integration Map L17, L79, L134, L176: todas mencionan Flyway |

### Bloque 5: Consistencia General

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| Todos los artefactos alineados | ❌ FAIL | 6 referencias residuales a `data.sql` en 4 documentos + ADR-002 desactualizado |
| No hay contradicciones entre documentos | ❌ FAIL | D7 vs D14 en Shared Context; System Landscape vs Delta Spec |
| Rutas de archivos referenciadas existen | ✅ PASS | ADR-001, ADR-002, ADR-003, context-map.md, TestJava2024_1.txt — todos verificados en disco |
| 5 casos de prueba cubiertos | ✅ PASS | Delta Spec §10.1: T1-T5 con datos completos |
| Cobertura 85% con JaCoCo definida | ✅ PASS | Delta Spec §9.1 L776-788: `minimum = "0.85"` |

---

## 2. Findings

### F1 — medium | Delta Spec: Scope table S7 stale reference
- **File:** `docs/specs/increment-001-pricing-api.md`, line 37
- **Exact string:** `| S7 | H2 + data.sql | Datos seed con 4 tarifas de ejemplo. |`
- **Conflict:** Delta Spec §7.2 (L579-612) y §8.2 (L673) describen Flyway.
- **Required change:** `| S7 | H2 + Flyway migrations | Datos seed con 4 tarifas de ejemplo. |`
- **Executor risk:** Executor lee el resumen de alcance y podría buscar `data.sql`.

### F2 — medium | Shared Context: D7 contradice D14
- **File:** `docs/specs/.working/pricing-sdd-context.md`, line 67
- **Exact string (D7):** `| D7 | Base de datos: H2 embebida en memoria, inicializada con \`data.sql\` | Requisito funcional. System Landscape §6 restricción 2. | — |`
- **Exact string (D14, L74):** `| D14 | Gestión de esquema con **Flyway** (JPA ddl-auto=validate) | Flyway es el único dueño del esquema. | — |`
- **Required change:** Update D7: `inicializada con migraciones Flyway (V1 DDL + V2 seed)`
- **Executor risk:** Dos decisiones contradictorias en el mismo documento.

### F3 — medium | System Landscape: Container h2-db references `data.sql`
- **File:** `docs/architecture/system-landscape.md`, line 68
- **Exact string:** `Se inicializa con \`data.sql\`.`
- **Conflict:** Delta Spec L673: "No se usan `schema.sql` ni `data.sql`."
- **Required change:** `Se inicializa con migraciones Flyway (V1 DDL + V2 seed).`
- **Executor risk:** Documento enterprise de arquitectura engaña sobre mecanismo de inicialización.

### F4 — medium | System Landscape: Restriction #3 references `data.sql`
- **File:** `docs/architecture/system-landscape.md`, line 112
- **Exact string:** `| 3 | Los datos seed son estáticos y se cargan desde \`data.sql\` al inicio. |`
- **Conflict:** Delta Spec L673: "No se usan `schema.sql` ni `data.sql`."
- **Required change:** `Los datos seed son estáticos y se cargan desde migraciones Flyway (V2) al inicio.`
- **Executor risk:** Mismo que F3.

### F5 — medium | Context Map: Seed data source references `data.sql`
- **File:** `docs/architecture/context-map.md`, line 120
- **Exact string:** `| Datos seed | \`data.sql\` (classpath del backend) | Pricing API |`
- **Conflict:** Delta Spec L673: "No se usan `schema.sql` ni `data.sql`."
- **Required change:** `| Datos seed | Flyway V2 migration (\`V2__seed_prices_data.sql\`) | Pricing API |`
- **Executor risk:** Artefacto enterprise engaña sobre fuente de datos.

### F6 — medium | ADR-002: Section 2 shows old `Optional<PriceEntity>` as primary
- **File:** `docs/architecture/decision-records/ADR-002-priority-resolution-strategy.md`, lines 36-43
- **Exact string:**
  ```
  Optional<PriceEntity> findTopByBrandIdAndProductIdAndStartDateBeforeAndEndDateAfterOrderByPriorityDesc(
      Long brandId, Long productId, LocalDateTime applicationDate, LocalDateTime applicationDate
  );
  ```
- **Conflict:** Delta Spec L397-417 usa `Page<PriceEntity>` + `Pageable` con `@Query` JPQL.
- **Required change:** Reemplazar el ejemplo de código con el enfoque `@Query` + `Page<PriceEntity>` + `Pageable` que coincide con la Delta Spec. Mover el `Optional<PriceEntity>` a alternativa rechazada o eliminarlo.
- **Executor risk:** ADR-002 es listado como artefacto autoritativo en Decomposition Contract §13.1. Executor podría implementar el enfoque antiguo.

### F7 — low | ADR-003: Historical references to `data.sql`
- **File:** `docs/architecture/decision-records/ADR-003-date-type-selection.md`, lines 34, 129
- **Exact strings:**
  - L34: `- **Seed data:** \`data.sql\` usa formato \`2020-06-14 00:00:00\` (sin offset).`
  - L129: `| data.sql | Literal SQL | \`'2020-06-14 00:00:00'\` |`
- **Required change:** Opcionalmente actualizar a `V2__seed_prices_data.sql`.
- **Executor risk:** Mínimo — el formato de fecha es correcto independientemente del nombre del archivo.

### F8 — low | Integration Map: I1 response field list omits `currency`
- **File:** `docs/architecture/integration-map.md`, line 32
- **Exact string:** `JSON con campos: \`productId\`, \`brandId\`, \`priceList\`, \`startDate\`, \`endDate\`, \`price\``
- **Conflict:** Mismo archivo L57 (ejemplo JSON incluye `"currency": "EUR"`) y OpenAPI L103-104 (`currency` es required).
- **Required change:** Agregar `currency` al listado de campos.
- **Executor risk:** Mínimo — el ejemplo JSON y OpenAPI son correctos.

---

## 3. Summary

| Severity | Count | IDs |
|----------|-------|-----|
| Blocker | 0 | — |
| High | 0 | — |
| Medium | 6 | F1, F2, F3, F4, F5, F6 |
| Low | 2 | F7, F8 |
| **Total** | **8** | |

### Root Cause
Las correcciones del Planner se aplicaron correctamente en los artefactos primarios (Delta Spec §6-8, OpenAPI, Integration Map ejemplos, Workspace Mapping). Sin embargo, **quedan referencias residuales a `data.sql`** en 4 documentos enterprise/de soporte (System Landscape ×2, Context Map ×1, Shared Context D7, Delta Spec S7, ADR-003 ×2) y el **ADR-002 no fue actualizado** para reflejar el enfoque `Page<PriceEntity>` + `Pageable` como solución primaria.

### Impact Assessment
- **Ningún finding es blocker.** La Delta Spec es autocontenida y lo suficientemente detallada para que Executor implemente correctamente siguiendo §6.2 (repository), §7.2 (Flyway), §8.1 (application.yml).
- Los findings medium son **stale references** en documentos de soporte que podrían causar confusión pero no impiden la implementación.
- El ADR-002 es el finding de mayor riesgo porque es listado como artefacto autoritativo en el Decomposition Contract.

---

## 4. Verdict

**`ready with minor changes`**

La especificación es implementable. Los 6 findings medium deben ser corregidos por el Planner antes de la consolidación en la Master Spec, pero no bloquean la descomposición de tareas ni la implementación.

**Acción requerida del Planner:**
1. Actualizar 6 referencias residuales a `data.sql` → `Flyway migrations` en System Landscape, Context Map, Shared Context D7, Delta Spec S7.
2. Actualizar ADR-002 §2 para mostrar `Page<PriceEntity>` + `Pageable` como enfoque primario.
3. Opcionalmente actualizar ADR-003 referencias históricas.
4. Opcionalmente agregar `currency` al listado de campos en Integration Map I1.
