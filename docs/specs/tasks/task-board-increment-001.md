# Task Board — Increment 001: Pricing API

> **Increment:** 001-pricing-api
> **Shared context:** `docs/specs/.working/pricing-sdd-context.md`
> **Delta spec:** `docs/specs/increment-001-pricing-api.md`
> **OpenAPI canonical:** `docs/api/pricing-api.yaml`
> **Created:** 2026-07-02
> **Agent:** task-decomposer

---

## Overview

| Total tasks | 16 |
|-------------|-----|
| **Executor tasks** | 13 |
| **DevOps-architect tasks** | 1 |
| **Git-executor tasks** | 1 |
| **Test-architect tasks** | 1 |
| **Default status** | `todo` |

---

## Dependency Graph

```
T01 (Gradle + Estructura)
 ├── T02 (OpenAPI Generator verificación)
 ├── T03 (Migraciones Flyway)
 │    └── T04 (Modelo de Dominio)
 │         └── T05 (Puertos de Aplicación)
 │              ├── T06 (Caso de Uso)
 │              │    ├── T07 (Adaptador JPA)
 │              │    └── T08 (Adaptador Web + Converter)
 │              │         └── T09 (Manejo de Errores)
 │              │              └── T10 (Config CORS)
 │              ├── T11 (Tests Unitarios — Dominio)
 │              └── T12 (Tests Unitarios — Caso de Uso)
 │                   └── T13 (Tests de Integración REST)
 │                        └── T14 (Verificación JaCoCo)
 ├── T15 (Docker Compose + Dockerfiles)
 └── T16 (README.md)
```

---

## Tarea 1: Configuración del proyecto Gradle y estructura base

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | Ninguna |
| **Archivos a crear** | `backend/build.gradle.kts`, `backend/settings.gradle.kts`, `backend/src/main/resources/application.yml` |
| **Directorios a crear** | `backend/src/main/java/com/esoluzion/pricing/domain/model/`, `backend/src/main/java/com/esoluzion/pricing/domain/exception/`, `backend/src/main/java/com/esoluzion/pricing/application/port/in/`, `backend/src/main/java/com/esoluzion/pricing/application/port/out/`, `backend/src/main/java/com/esoluzion/pricing/application/service/`, `backend/src/main/java/com/esoluzion/pricing/application/model/`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/out/persistence/`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/config/`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/exception/`, `backend/src/main/resources/db/migration/`, `backend/src/test/java/` |

**Goal:** Establecer la estructura de build y configuración del proyecto backend con todas las dependencias necesarias.

**Scope:**
- `build.gradle.kts` con plugins (Spring Boot 3.3+, Spring Dependency Management 1.1+, OpenAPI Generator 7.6.0, JaCoCo), todas las dependencias (spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, flyway-core, h2, mapstruct 1.5.5.Final, spring-boot-starter-test, junit-platform-launcher), configuración OpenAPI Generator (generatorName=spring, inputSpec apuntando a `docs/api/pricing-api.yaml`, apiPackage, modelPackage, configOptions con interfaceOnly=true, useSpringBoot3=true, useJakartaEe=true, dateLibrary=java8-localdatetime, skipDefaultInterface=true, useTags=true, generateBuilders=true, library=spring-boot), sourceSets para incluir `build/generated/src/main/java`, compileJava dependsOn openApiGenerate, configuración JaCoCo con exclusions y threshold 0.85, check dependsOn jacocoTestCoverageVerification.
- `settings.gradle.kts` con rootProject.name = "pricing-api".
- `application.yml` con server.port=8080, spring.application.name=pricing-api, datasource H2 in-memory (url=jdbc:h2:mem:pricingdb;DB_CLOSE_DELAY=-1), jpa ddl-auto=validate, database-platform=org.hibernate.dialect.H2Dialect, flyway enabled=true, locations=classpath:db/migration, baseline-on-migrate=true, h2 console enabled=true en /h2-console, logging levels.

**Out of scope:** Código fuente Java, migraciones SQL, Dockerfiles, README.

**Implementation notes:**
- Usar exactamente las versiones y coordenadas de la Delta Spec §9.
- El inputSpec debe ser `"${rootDir}/../docs/api/pricing-api.yaml"` para resolver correctamente desde `backend/`.
- Las exclusions JaCoCo deben ser: `**/dto/**`, `**/entity/**`, `**/config/**`, `**/exceptions/**`, `**/exception/**`, `**/*MapperImpl*`, `**/*Application*`.
- La tarea `compileJava` debe depender de `openApiGenerate` para que la generación de código ocurra antes de compilar.
- `settings.gradle.kts` debe incluir `rootProject.name = "pricing-api"`.
- No crear `PricingApiApplication.java` — se crea en T8 con el contexto adecuado.

**Edge cases:** El plugin OpenAPI Generator requiere que el archivo `docs/api/pricing-api.yaml` exista. Verificar que el path relativo `${rootDir}/../docs/api/pricing-api.yaml` resuelve correctamente (rootDir es `backend/`, por lo que `../docs/` apunta a `docs/` en raíz).

**Done criteria:**
- `build.gradle.kts` compila sin errores de sintaxis (verificar con `gradle tasks`).
- `settings.gradle.kts` define correctamente el proyecto.
- `application.yml` es sintácticamente válido.
- Todos los directorios de paquetes existen.

**Verificación:**
```bash
cd backend && gradle tasks --no-daemon 2>&1 | head -20
# Debe listar tareas disponibles sin errores de sintaxis
```
```bash
cd backend && gradle dependencies --configuration runtimeClasspath --no-daemon 2>&1 | grep -E "(spring-boot|flyway|h2|mapstruct)"
# Debe mostrar las dependencias configuradas
```

**Handoff context:** El `build.gradle.kts` contiene las configuraciones de OpenAPI Generator y JaCoCo. Tareas T2 y T14 extienden/verifican estas configuraciones.

---

## Tarea 2: OpenAPI Generator — copia runtime y verificación de generación

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | todo |
| **Dependencias** | T1 |
| **Archivos a crear** | `backend/src/main/resources/openapi.yaml` |
| **Archivos a verificar** | `backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/api/PriceApi.java`, `backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/PriceResponse.java`, `backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/ApiErrorResponse.java`, `backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/ApiErrorDetail.java` |

**Goal:** Verificar que el plugin OpenAPI Generator produce correctamente las interfaces y DTOs desde el contrato canónico.

**Scope:**
- Copiar `docs/api/pricing-api.yaml` → `backend/src/main/resources/openapi.yaml` (copia fiel, no fuente de verdad).
- Ejecutar `gradle openApiGenerate` para generar código.
- Verificar que los 4 archivos generados existen: `PriceApi.java`, `PriceResponse.java`, `ApiErrorResponse.java`, `ApiErrorDetail.java`.
- Verificar que `PriceApi.java` contiene el método `getApplicablePrice` con los parámetros esperados (LocalDateTime applicationDate, Long productId, Long brandId) y retorna `ResponseEntity<PriceResponse>`.
- Verificar que `PriceResponse.java` tiene los 7 campos: productId (Long), brandId (Long), priceList (Integer), startDate (LocalDateTime), endDate (LocalDateTime), price (BigDecimal), currency (String).
- Verificar que `ApiErrorResponse.java` tiene los campos: timestamp, status, error, code, message, path, traceId, details (List<ApiErrorDetail>).
- Verificar que `compileJava` depende correctamente de `openApiGenerate` (de T1).

**Out of scope:** Modificar el OpenAPI canónico, implementar controllers, modificar DTOs generados.

**Implementation notes:**
- La copia runtime es literal: mismo contenido que `docs/api/pricing-api.yaml`.
- El directorio `build/generated/` se crea automáticamente por el plugin.
- Si la generación falla, verificar que el inputSpec en build.gradle.kts resuelve correctamente.
- Los DTOs generados usan anotaciones Jakarta (`jakarta.validation`, `jakarta.annotation`) por `useJakartaEe: true`.
- Los builders se generan por `generateBuilders: true`.

**Edge cases:**
- Si `build/generated/` ya existe de una ejecución anterior, `gradle clean openApiGenerate` debe regenerar limpiamente.
- El campo `trace_id` en OpenAPI usa snake_case. El generador puede convertirlo a `traceId` (camelCase Java). Verificar y documentar que el JSON se serializa con `@JsonProperty("trace_id")` o configurar `spring.jackson.property-naming-strategy=SNAKE_CASE` si es necesario.

**Done criteria:**
- `backend/src/main/resources/openapi.yaml` es copia fiel del canónico.
- `gradle openApiGenerate` se ejecuta sin errores.
- Los 4 archivos generados existen en las rutas esperadas.
- `PriceApi.java` tiene la firma `ResponseEntity<PriceResponse> getApplicablePrice(...)`.

**Verificación:**
```bash
cd backend && gradle clean openApiGenerate --no-daemon
```
```bash
ls -la backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/api/PriceApi.java
ls -la backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/PriceResponse.java
ls -la backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/ApiErrorResponse.java
ls -la backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/dto/ApiErrorDetail.java
```
```bash
grep "getApplicablePrice" backend/build/generated/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/api/PriceApi.java
```

**Handoff context:** Los DTOs generados (`PriceResponse`, `ApiErrorResponse`, `ApiErrorDetail`) son usados por T8 (PriceController) y T9 (GlobalExceptionHandler). La interfaz `PriceApi` es implementada por T8.

---

## Tarea 3: Migraciones Flyway

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | todo |
| **Dependencias** | T1 |
| **Archivos a crear** | `backend/src/main/resources/db/migration/V1__create_prices_table.sql`, `backend/src/main/resources/db/migration/V2__seed_prices_data.sql` |

**Goal:** Crear las migraciones Flyway que definen el esquema de la tabla `prices` y los datos semilla.

**Scope:**
- `V1__create_prices_table.sql`: `CREATE TABLE prices` con 9 columnas exactamente como en Delta Spec §7.2 (id BIGINT AUTO_INCREMENT PK, brand_id BIGINT NOT NULL, start_date TIMESTAMP NOT NULL, end_date TIMESTAMP NOT NULL, price_list INT NOT NULL, product_id BIGINT NOT NULL, priority INT NOT NULL, price DECIMAL(10,2) NOT NULL, curr VARCHAR(3) NOT NULL). Incluir `CREATE INDEX idx_prices_brand_product_dates ON prices(brand_id, product_id, start_date, end_date)`.
- `V2__seed_prices_data.sql`: 4 registros INSERT con los datos exactos de Delta Spec §7.2.

**Out of scope:** Modificar el esquema, añadir tablas adicionales, triggers, stored procedures.

**Implementation notes:**
- Usar `TIMESTAMP` (no `DATETIME`) para H2.
- Las fechas van sin zona horaria: `'2020-06-14 00:00:00'` (no incluir `Z` ni offset).
- El índice compuesto cubre brand_id, product_id, start_date, end_date para optimizar la query del repository.
- La convención Flyway: `V{numero}__{descripcion}.sql` (doble underscore después del número).

**Edge cases:**
- Si H2 no soporta `DECIMAL(10,2)`, usar `NUMERIC(10,2)` como alternativa. Verificar compatibilidad.
- Si hay error de sintaxis en los INSERT, verificar que el formato de fecha `'YYYY-MM-DD HH:MM:SS'` es aceptado por H2.

**Done criteria:**
- `V1__create_prices_table.sql` crea la tabla con las 9 columnas exactas y el índice.
- `V2__seed_prices_data.sql` inserta los 4 registros con los valores exactos de la spec.
- Ambas migraciones son sintácticamente correctas para H2.

**Verificación:**
```bash
# Verificar que los archivos existen y tienen contenido
wc -l backend/src/main/resources/db/migration/V1__create_prices_table.sql
wc -l backend/src/main/resources/db/migration/V2__seed_prices_data.sql
```
```bash
# Verificar keywords clave
grep "CREATE TABLE prices" backend/src/main/resources/db/migration/V1__create_prices_table.sql
grep "CREATE INDEX" backend/src/main/resources/db/migration/V1__create_prices_table.sql
grep "INSERT INTO prices" backend/src/main/resources/db/migration/V2__seed_prices_data.sql
```

**Handoff context:** Las migraciones son ejecutadas por Flyway al iniciar la aplicación. Los tests de integración (T13) dependen de estos datos para los 5 casos T1-T5.

---

## Tarea 4: Modelo de Dominio

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | T1 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/domain/model/Price.java`, `backend/src/main/java/com/esoluzion/pricing/domain/model/DateRange.java`, `backend/src/main/java/com/esoluzion/pricing/domain/exception/PriceNotFoundException.java` |

**Goal:** Implementar las clases puras del dominio sin dependencias de frameworks.

**Scope:**

1. **`Price.java`** — Entidad de dominio inmutable con campos: `productId` (Long), `brandId` (Long), `priceList` (Integer), `dateRange` (DateRange), `priority` (Integer), `price` (BigDecimal), `currency` (String). Constructor con validaciones: price ≥ 0 (lanzar IllegalArgumentException si no), currency != null y length == 3, priority ≥ 0, dateRange != null, productId > 0, brandId > 0. Sin setters. Sin anotaciones JPA/Spring.

2. **`DateRange.java`** — Value Object inmutable con `startDate` (LocalDateTime) y `endDate` (LocalDateTime). Validación: startDate ≤ endDate. Método `boolean contains(LocalDateTime instant)` que retorna true si `!startDate.isAfter(instant) && !endDate.isBefore(instant)`.

3. **`PriceNotFoundException.java`** — Extiende `RuntimeException`. Mensaje hardcoded: `"No applicable price found for the given parameters"`. Sin dependencias de Spring/HTTP.

**Out of scope:** Anotaciones JPA, DTOs de transporte, lógica de persistencia, servicios de aplicación.

**Implementation notes:**
- Usar `java.util.Objects.requireNonNull()` para validaciones de null.
- Los campos de `Price` son `private final`.
- `DateRange` implementa correctamente `equals()` y `hashCode()` basados en `startDate` y `endDate`.
- No usar Lombok. Escribir constructores, getters, equals, hashCode manualmente.
- `DateRange.contains()` usa `LocalDateTime.isAfter()`/`isBefore()` para comparación precisa (el método `compareTo` también funciona pero `isAfter`/`isBefore` es más legible).

**Edge cases:**
- `DateRange.contains(null)` → lanzar NullPointerException (consistente con requireNonNull interno).
- `DateRange(startDate, endDate)` con startDate después de endDate → lanzar IllegalArgumentException.
- `Price(null, ...)` → NullPointerException vía requireNonNull.

**Done criteria:**
- `Price.java` es inmutable, sin dependencias externas, con todas las validaciones.
- `DateRange.java` es inmutable, con `contains()` correcto y validación startDate ≤ endDate.
- `PriceNotFoundException.java` extiende RuntimeException con el mensaje especificado.
- Ningún archivo en `domain/` importa clases de Spring, JPA, Jackson, o HTTP.

**Verificación:**
```bash
# Verificar que domain/ no tiene dependencias de frameworks
grep -r "import org.springframework" backend/src/main/java/com/esoluzion/pricing/domain/ && echo "FAIL: Spring dependency in domain!" || echo "OK: No Spring in domain"
grep -r "import javax.persistence\|import jakarta.persistence" backend/src/main/java/com/esoluzion/pricing/domain/ && echo "FAIL: JPA dependency in domain!" || echo "OK: No JPA in domain"
```
```bash
# Compilación aislada (solo domain, sin dependencias externas)
cd backend && gradle compileJava --no-daemon 2>&1 | tail -5
```

**Handoff context:** `Price` y `DateRange` son usados por T5 (puertos), T6 (caso de uso), T7 (JPA adapter necesita mapear desde/hacia Price). `PriceNotFoundException` es usada por T6 y T9.

---

## Tarea 5: Puertos de Aplicación

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | todo |
| **Dependencias** | T4 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/application/port/in/GetApplicablePriceUseCase.java`, `backend/src/main/java/com/esoluzion/pricing/application/port/out/PriceRepositoryPort.java`, `backend/src/main/java/com/esoluzion/pricing/application/model/PriceQuery.java`, `backend/src/main/java/com/esoluzion/pricing/application/model/PriceResult.java` |

**Goal:** Definir las interfaces de puertos (input/output) y los objetos de transporte de la capa de aplicación.

**Scope:**

1. **`PriceQuery.java`** — Record con campos: `applicationDate` (LocalDateTime, non-null), `productId` (Long, non-null, > 0), `brandId` (Long, non-null, > 0). Validaciones en constructor canónico compacto.

2. **`PriceResult.java`** — Record con campos: `productId` (Long), `brandId` (Long), `priceList` (Integer), `startDate` (LocalDateTime), `endDate` (LocalDateTime), `price` (BigDecimal), `currency` (String).

3. **`GetApplicablePriceUseCase.java`** — Interface con método `PriceResult getApplicablePrice(PriceQuery query)`. Paquete `application.port.in`.

4. **`PriceRepositoryPort.java`** — Interface con método `Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate)`. Paquete `application.port.out`.

**Out of scope:** Implementaciones (T6, T7), anotaciones Spring en las interfaces.

**Implementation notes:**
- `PriceQuery` y `PriceResult` usan Java `record` (Java 21+). Validaciones en constructor compacto.
- `PriceQuery` lanza `IllegalArgumentException` si algún campo es null o IDs ≤ 0.
- `PriceResult` no tiene validaciones (es un DTO de salida inmutable).
- Las interfaces de puerto no llevan anotaciones Spring. Son Java puro.
- `Optional<Price>` en `PriceRepositoryPort` — Price es la entidad de dominio (T4), no la entidad JPA.

**Edge cases:**
- `PriceQuery` con `productId=0` → IllegalArgumentException.
- `PriceQuery` con `brandId=-1` → IllegalArgumentException.

**Done criteria:**
- `PriceQuery` es un record con validaciones en constructor compacto.
- `PriceResult` es un record sin validaciones.
- `GetApplicablePriceUseCase` es una interfaz pura (sin anotaciones).
- `PriceRepositoryPort` es una interfaz pura (sin anotaciones).
- La capa `application/` no depende de Spring, JPA, ni HTTP.

**Verificación:**
```bash
grep -r "import org.springframework\|import javax.persistence\|import jakarta.persistence\|import org.springframework.web" backend/src/main/java/com/esoluzion/pricing/application/ && echo "FAIL: Framework dependency in application!" || echo "OK: Clean application layer"
```
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -5
```

**Handoff context:** `GetApplicablePriceUseCase` es implementado por T6. `PriceRepositoryPort` es implementado por T7. `PriceQuery` y `PriceResult` son usados por T6 y T8.

---

## Tarea 6: Caso de Uso — GetApplicablePriceService

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | todo |
| **Dependencias** | T4, T5 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/application/service/GetApplicablePriceService.java` |

**Goal:** Implementar el servicio de aplicación que orquesta la consulta de precio aplicable.

**Scope:**
- Clase `GetApplicablePriceService` anotada con `@Service` que implementa `GetApplicablePriceUseCase`.
- Inyección por constructor de `PriceRepositoryPort`.
- Método `getApplicablePrice(PriceQuery query)` anotado con `@Transactional(readOnly = true)`.
- Lógica (Happy path): (1) validar query no null, (2) llamar `priceRepository.findApplicablePrice(query.brandId(), query.productId(), query.applicationDate())`, (3) si `Optional` está vacío → lanzar `PriceNotFoundException`, (4) mapear `Price` → `PriceResult`, (5) retornar `PriceResult`.
- Failure paths: query null → `IllegalArgumentException`, repository retorna empty → `PriceNotFoundException`.

**Out of scope:** Validaciones de dominio (ya están en T4). Manejo HTTP de excepciones (T9). Mapeo a DTOs de API (T8).

**Implementation notes:**
- El servicio no conoce la infraestructura. Solo depende de `PriceRepositoryPort` (interfaz) y las clases del dominio.
- La anotación `@Service` de Spring es aceptable en la capa de aplicación (es un stereotype de Spring para el contenedor DI).
- El mapeo `Price` → `PriceResult` se hace inline (no requiere mapper externo porque ambos son objetos planos):
  ```java
  return new PriceResult(
      price.getProductId(),
      price.getBrandId(),
      price.getPriceList(),
      price.getDateRange().getStartDate(),
      price.getDateRange().getEndDate(),
      price.getPrice(),
      price.getCurrency()
  );
  ```

**Edge cases:**
- Si el repository lanza una excepción inesperada, se propaga (no se captura aquí — lo maneja T9 como 500).
- El método `getApplicablePrice` recibe `PriceQuery` que ya fue validado en su constructor (T5). Aún así, se valida query != null aquí por defensa en profundidad.

**Done criteria:**
- `GetApplicablePriceService` compila correctamente.
- Implementa `GetApplicablePriceUseCase`.
- Usa `@Transactional(readOnly = true)`.
- Delega correctamente al `PriceRepositoryPort` y mapea `Price` → `PriceResult`.
- Lanza `PriceNotFoundException` cuando el repository retorna `Optional.empty()`.

**Verificación:**
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -5
```
```bash
# Verificar que el servicio no depende de infraestructura
grep "import com.esoluzion.pricing.infrastructure" backend/src/main/java/com/esoluzion/pricing/application/service/GetApplicablePriceService.java && echo "FAIL: Infrastructure dependency!" || echo "OK: No infrastructure dependency"
```

**Handoff context:** `GetApplicablePriceService` es usado por T8 (PriceController). Los tests unitarios del caso de uso están en T12.

---

## Tarea 7: Adaptador JPA — Persistencia

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | T3, T4, T5 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/out/persistence/PriceEntity.java`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/out/persistence/PriceJpaRepository.java`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/out/persistence/PriceInfrastructureMapper.java`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/out/persistence/PriceJpaAdapter.java` |

**Goal:** Implementar el driven adapter de persistencia JPA que conecta con H2 y traduce entre entidades JPA y dominio.

**Scope:**

1. **`PriceEntity.java`** — Entidad JPA (`@Entity`, `@Table(name = "prices")`) con 9 columnas exactamente como en Delta Spec §6.2 (id, brandId, startDate, endDate, priceList, productId, priority, price, currency). `@Id` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Constructor protegido (JPA) + factory method o builder. Getters. Sin setters públicos.

2. **`PriceJpaRepository.java`** — Interface `@Repository` que extiende `JpaRepository<PriceEntity, Long>`. Método JPQL `findApplicablePrice` como en Delta Spec §6.2: usa `Page<PriceEntity>` con `Pageable` (NO `Optional<T>`). Query JPQL con `WHERE brandId = :brandId AND productId = :productId AND startDate <= :applicationDate AND endDate >= :applicationDate ORDER BY priority DESC`.

3. **`PriceInfrastructureMapper.java`** — Interface `@Mapper(componentModel = "spring")` de MapStruct con método `Price toDomain(PriceEntity entity)` que mapea `startDate` y `endDate` a un `DateRange` vía `expression = "java(new DateRange(entity.getStartDate(), entity.getEndDate()))"`.

4. **`PriceJpaAdapter.java`** — Clase `@Component` que implementa `PriceRepositoryPort`. Inyecta `PriceJpaRepository` y `PriceInfrastructureMapper` por constructor. Método `findApplicablePrice` que llama al repository con `PageRequest.of(0, 1)`, extrae el primer resultado con `.getContent().stream().findFirst()` y lo mapea con `mapper::toDomain`.

**Out of scope:** Migraciones (T3), repositorios para otras entidades, CRUD operations.

**Implementation notes:**
- Los nombres de columnas en `PriceEntity` usan snake_case (`@Column(name = "brand_id")`).
- `BigDecimal` con `precision = 10, scale = 2` en `price`.
- `String` con `length = 3` en `currency` (columna `curr`).
- MapStruct requiere `annotationProcessor` en build.gradle.kts (ya configurado en T1). Verificar que la dependencia `mapstruct-processor` está presente.
- El JPQL usa `Page<PriceEntity>` + `PageRequest.of(0, 1)` en lugar de `Optional<T>` para garantizar `LIMIT 1` correcto y evitar `IncorrectResultSizeDataAccessException` en caso de empate de prioridad.

**Edge cases:**
- Si no hay resultados, `Page.getContent()` retorna lista vacía → `stream().findFirst()` retorna `Optional.empty()`.
- Si hay múltiples registros con la misma prioridad, `PageRequest.of(0, 1)` solo retorna uno.
- MapStruct necesita compilación con annotation processor. Si el mapper no se genera, verificar que `annotationProcessor` está en `dependencies` (no solo en `implementation`).

**Done criteria:**
- `PriceEntity` mapea correctamente las 9 columnas de la tabla `prices`.
- `PriceJpaRepository` tiene la query JPQL correcta con `ORDER BY priority DESC` y `Pageable`.
- `PriceInfrastructureMapper` genera el mapper (verificar en `build/generated/`).
- `PriceJpaAdapter` implementa `PriceRepositoryPort` y usa `PageRequest.of(0, 1)`.
- La aplicación arranca con `ddl-auto: validate` y Flyway ejecuta las migraciones correctamente.

**Verificación:**
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -10
```
```bash
# Verificar que MapStruct generó el mapper
find backend/build -name "PriceInfrastructureMapperImpl.java" 2>/dev/null && echo "Mapper generated OK" || echo "WARNING: Mapper not found"
```
```bash
# Verificar que la aplicación arranca (requiere T8+T9 también para el contexto completo)
# Esto se verifica completamente en T13 (tests de integración)
```

**Handoff context:** El adapter JPA es el único driven adapter. Los tests de integración (T13) verifican la query real contra H2 con los datos seed.

---

## Tarea 8: Adaptador Web — PriceController + Converter + Application Entry Point

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | T2, T4, T5, T6 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/infrastructure/adapter/in/web/PriceController.java`, `backend/src/main/java/com/esoluzion/pricing/infrastructure/config/StringToLocalDateTimeConverter.java`, `backend/src/main/java/com/esoluzion/pricing/PricingApiApplication.java` |

**Goal:** Implementar el driving adapter REST, el converter de fechas para compatibilidad con sufijo Z, y la clase principal de Spring Boot.

**Scope:**

1. **`PriceController.java`** — Clase `@RestController` que implementa `PriceApi` (interfaz generada por OpenAPI Generator en T2). Inyecta `GetApplicablePriceUseCase` por constructor. Método `getApplicablePrice` que: (1) crea `PriceQuery` con los parámetros, (2) llama al use case, (3) mapea `PriceResult` → `PriceResponse` (DTO generado), (4) retorna `ResponseEntity.ok(response)`. Usa el builder generado para `PriceResponse`.

2. **`StringToLocalDateTimeConverter.java`** — Clase `@Component` que implementa `Converter<String, LocalDateTime>`. Lógica: si el string termina en `Z` o `z`, eliminar el sufijo. Parsear con `DateTimeFormatter.ISO_LOCAL_DATE_TIME`. Maneja strings con y sin sufijo Z.

3. **`PricingApiApplication.java`** — Clase `@SpringBootApplication` en el paquete raíz `com.esoluzion.pricing`. Contiene el método `main` estándar.

**Out of scope:** Manejo de excepciones HTTP (T9), CORS (T10), validaciones adicionales de parámetros (las validaciones de query params vienen de la interfaz generada y del converter).

**Implementation notes:**
- `PriceController` NO repite anotaciones `@GetMapping`, `@RequestParam` — estas vienen de la interfaz generada `PriceApi`. El controller solo tiene `@RestController` y `@Override`.
- El mapeo `PriceResult` → `PriceResponse` usa el builder generado:
  ```java
  PriceResponse response = new PriceResponse()
      .productId(result.productId())
      .brandId(result.brandId())
      .priceList(result.priceList())
      .startDate(result.startDate())
      .endDate(result.endDate())
      .price(result.price())
      .currency(result.currency());
  ```
- El converter `StringToLocalDateTimeConverter` se registra automáticamente por Spring Boot al ser `@Component` e implementar `Converter<String, LocalDateTime>`.
- `PricingApiApplication.java` debe estar en el paquete `com.esoluzion.pricing` (raíz) para que el component scanning cubra todos los subpaquetes.

**Edge cases:**
- Si el query param `applicationDate` no incluye `Z`, el converter igual debe parsearlo correctamente (el `DateTimeFormatter.ISO_LOCAL_DATE_TIME` espera el formato sin Z).
- Si el formato es inválido, el converter lanza `DateTimeParseException` → Spring lo convierte en error 400 (manejado por T9 o por el handler por defecto de Spring).
- La interfaz generada `PriceApi` puede estar en un paquete diferente. Verificar el import exacto desde `build/generated/`.

**Done criteria:**
- `PriceController` compila e implementa `PriceApi` correctamente.
- `StringToLocalDateTimeConverter` convierte strings ISO-8601 con y sin sufijo Z.
- `PricingApiApplication.java` compila y está en el paquete raíz.
- `gradle compileJava` pasa sin errores.

**Verificación:**
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -10
```
```bash
# Verificar que la aplicación arranca
cd backend && gradle bootRun --no-daemon &
sleep 15
curl -s "http://localhost:8080/api/prices?applicationDate=2020-06-14T10:00:00Z&productId=35455&brandId=1" | python3 -m json.tool
kill %1 2>/dev/null
```

**Handoff context:** El controller es el punto de entrada HTTP. Los tests de integración (T13) ejercitan este endpoint. El converter es necesario para que los query params con sufijo Z sean parseados correctamente sin cambiar a `OffsetDateTime`.

---

## Tarea 9: Manejo de Errores — GlobalExceptionHandler

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | T2, T4 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/infrastructure/exception/GlobalExceptionHandler.java` |

**Goal:** Implementar el handler global de excepciones que traduce excepciones de dominio y de Spring a respuestas `ApiErrorResponse` estándar.

**Scope:**
Clase `@RestControllerAdvice` con 7 handlers de excepción como en Delta Spec §6.3:

1. `PriceNotFoundException` → `404 NOT_FOUND`, code `PRICE_NOT_FOUND`.
2. `MethodArgumentNotValidException` / `ConstraintViolationException` → `400 BAD_REQUEST`, code `VALIDATION_ERROR`.
3. `HttpMessageNotReadableException` → `400 BAD_REQUEST`, code `INVALID_REQUEST_BODY`.
4. `TypeMismatchException` → `400 BAD_REQUEST`, code `VALIDATION_ERROR`.
5. `MissingServletRequestParameterException` → `400 BAD_REQUEST`, code `VALIDATION_ERROR`.
6. `IllegalArgumentException` → `400 BAD_REQUEST`, code `VALIDATION_ERROR`.
7. `Exception` (fallback) → `500 INTERNAL_SERVER_ERROR`, code `INTERNAL_ERROR`.

Cada handler:
- Construye `ApiErrorResponse` usando el builder generado por OpenAPI Generator.
- Incluye `timestamp` en UTC ISO-8601.
- Incluye `path` desde `HttpServletRequest.getRequestURI()`.
- Incluye `traceId` generado con `UUID.randomUUID().toString().replace("-", "").substring(0, 16)`.
- Incluye `details` con `List<ApiErrorDetail>` relevante (puede estar vacío).
- Para el fallback 500: **NO** exponer `exception.getMessage()`. Usar mensaje genérico: `"An unexpected error occurred."`.
- Loguea el error real (incluyendo stack trace) en el backend con el `traceId`.

**Out of scope:** Páginas de error HTML, Whitelabel Error Page (debe estar deshabilitada por el handler).

**Implementation notes:**
- Los DTOs `ApiErrorResponse` y `ApiErrorDetail` se importan desde `com.esoluzion.pricing.infrastructure.adapter.in.web.dto` (generados en T2).
- Para `timestamp`: usar `OffsetDateTime.now(ZoneOffset.UTC).toString()` o `Instant.now().toString()`.
- Para el handler de validación (`MethodArgumentNotValidException`), extraer field errors y mapearlos a `ApiErrorDetail`.
- El handler de `TypeMismatchException` captura errores cuando un query param no se puede convertir al tipo esperado.
- El handler fallback debe ser el último `@ExceptionHandler` para que los handlers específicos tengan prioridad.

**Edge cases:**
- Si el `HttpServletRequest` es null (poco probable en práctica), el path debe ser `"unknown"`.
- `ApiErrorDetail.rejectedValue` es nullable. Para valores que no exponen datos sensibles, incluir el valor rechazado.
- El método `handleValidation` debe manejar tanto `MethodArgumentNotValidException` como `ConstraintViolationException` con la misma lógica.

**Done criteria:**
- 7 handlers implementados con los status codes y error codes correctos.
- Fallback 500 no expone mensajes internos.
- `traceId` único por request.
- `details` nunca es null (lista vacía si no hay detalles).
- No hay Whitelabel Error Page (todas las excepciones son capturadas).

**Verificación:**
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -10
```
```bash
# Verificar que no hay referencias a Whitelabel
grep -r "Whitelabel\|error.html\|/error" backend/src/main/java/com/esoluzion/pricing/ && echo "WARNING: Possible Whitelabel reference" || echo "OK: No Whitelabel references"
```

**Handoff context:** El handler es verificado por los tests de integración (T13) en los casos de error E1-E5.

---

## Tarea 10: Configuración CORS

| Campo | Valor |
|-------|-------|
| **Agente** | executor |
| **Status** | done |
| **Dependencias** | T1 |
| **Archivos a crear** | `backend/src/main/java/com/esoluzion/pricing/infrastructure/config/CorsConfig.java` |

**Goal:** Configurar CORS para permitir requests desde el frontend (localhost:3000).

**Scope:**
- Clase `@Configuration` que implementa `WebMvcConfigurer`.
- Método `addCorsMappings(CorsRegistry registry)`:
  - `registry.addMapping("/api/**")`
  - `.allowedOrigins("http://localhost:3000")`
  - `.allowedMethods("GET")`
  - `.allowedHeaders("*")`

**Out of scope:** Configuración de CORS para otros orígenes, métodos POST/PUT/DELETE.

**Implementation notes:**
- No se requiere `@EnableWebMvc` — `WebMvcConfigurer` se detecta automáticamente en Spring Boot.
- El path pattern `/api/**` cubre el endpoint `/api/prices`.
- Solo se permite GET porque es el único método expuesto en este incremento.

**Edge cases:** Ninguno significativo. Si en el futuro se añaden más métodos HTTP, actualizar `allowedMethods`.

**Done criteria:**
- `CorsConfig.java` compila.
- Implementa `WebMvcConfigurer` con la configuración especificada.

**Verificación:**
```bash
cd backend && gradle compileJava --no-daemon 2>&1 | tail -5
```

**Handoff context:** La configuración CORS es necesaria para el frontend React (incremento 002). No afecta los tests de integración (T13) que se ejecutan en el mismo origen.

---

## Tarea 11: Tests Unitarios — Dominio

| Campo | Valor |
|-------|-------|
| **Agente** | test-architect |
| **Status** | todo |
| **Dependencias** | T4 |
| **Archivos a crear** | `backend/src/test/java/com/esoluzion/pricing/domain/model/PriceTest.java`, `backend/src/test/java/com/esoluzion/pricing/domain/model/DateRangeTest.java` |

**Goal:** Implementar tests unitarios para la entidad `Price` y el value object `DateRange`.

**Scope:**

**`DateRangeTest.java`** — 5 tests según Delta Spec §10.3:
- U1: `contains()` fecha dentro del rango → true.
- U2: `contains()` fecha fuera del rango → false.
- U3: `contains()` fecha en startDate exacto → true (límite inclusivo).
- U4: `contains()` fecha en endDate exacto → true (límite inclusivo).
- U5: Constructor con startDate > endDate → lanza excepción.

**`PriceTest.java`** — Tests de construcción y validación:
- Construcción con todos los campos válidos → éxito.
- `price` negativo → lanza `IllegalArgumentException`.
- `currency` null → lanza `NullPointerException`.
- `currency` longitud != 3 → lanza `IllegalArgumentException`.
- `priority` negativo → lanza `IllegalArgumentException`.
- `productId` ≤ 0 → lanza `IllegalArgumentException`.
- `brandId` ≤ 0 → lanza `IllegalArgumentException`.
- `dateRange` null → lanza `NullPointerException`.
- Inmutabilidad — verificar que no hay setters (vía reflexión o intento de modificación).

**Out of scope:** Tests de integración (T13), tests del caso de uso (T12).

**Implementation notes:**
- Usar JUnit 5 (`@Test`, `assertThrows`, `assertEquals`).
- Usar AssertJ para aserciones fluidas (`assertThat`).
- No usar Mockito (no hay dependencias que mockear).
- No cargar contexto de Spring (tests unitarios puros).

**Edge cases:**
- `DateRange.contains()` con el instante exactamente igual a startDate → debe retornar true.
- `DateRange.contains()` con el instante exactamente igual a endDate → debe retornar true.

**Done criteria:**
- 5 tests de `DateRange` pasan.
- Al menos 8 tests de `Price` pasan.
- Cobertura del paquete `domain.model` cercana al 100%.

**Verificación:**
```bash
cd backend && gradle test --tests "com.esoluzion.pricing.domain.model.*" --no-daemon 2>&1 | tail -10
```

**Handoff context:** Los tests unitarios del dominio contribuyen a la cobertura JaCoCo (T14). Son ejecutados antes de los tests de integración.

---

## Tarea 12: Tests Unitarios — Caso de Uso

| Campo | Valor |
|-------|-------|
| **Agente** | test-architect |
| **Status** | todo |
| **Dependencias** | T4, T5, T6 |
| **Archivos a crear** | `backend/src/test/java/com/esoluzion/pricing/application/service/GetApplicablePriceServiceTest.java` |

**Goal:** Implementar tests unitarios para `GetApplicablePriceService` con mock del repository.

**Scope:** 3 tests según Delta Spec §10.4:

- UC1: Repository retorna `Price` → el servicio retorna `PriceResult` con los mismos valores (verificar mapeo campo por campo).
- UC2: Repository retorna `Optional.empty()` → el servicio lanza `PriceNotFoundException`.
- UC3: Query con `productId` null → el servicio lanza `IllegalArgumentException` (o se lanza al construir `PriceQuery` en T5).

**Out of scope:** Tests del repository real (eso es integración — T13), tests de controllers.

**Implementation notes:**
- Usar Mockito para mockear `PriceRepositoryPort`.
- Usar `@ExtendWith(MockitoExtension.class)`.
- Inyectar el mock en `GetApplicablePriceService` vía constructor.
- No cargar contexto de Spring (`@SpringBootTest` no necesario).
- Para UC1, construir un `Price` de dominio completo y verificar que cada campo del `PriceResult` coincide.

**Edge cases:**
- Si el `Price` de dominio tiene `dateRange`, el `PriceResult` debe tener `startDate` y `endDate` planos (no el objeto DateRange).
- El test UC3 verifica que `PriceQuery` rechaza valores null en su constructor (validación de T5), o alternativamente que el servicio lanza excepción cuando recibe query con campos null.

**Done criteria:**
- 3 tests pasan.
- El mock de `PriceRepositoryPort` se comporta correctamente.
- UC1 verifica el mapeo completo de `Price` → `PriceResult`.

**Verificación:**
```bash
cd backend && gradle test --tests "com.esoluzion.pricing.application.service.GetApplicablePriceServiceTest" --no-daemon 2>&1 | tail -10
```

**Handoff context:** Contribuye a la cobertura JaCoCo (T14) para la capa de aplicación.

---

## Tarea 13: Tests de Integración REST

| Campo | Valor |
|-------|-------|
| **Agente** | test-architect |
| **Status** | todo |
| **Dependencias** | T3, T4, T5, T6, T7, T8, T9, T10 |
| **Archivos a crear** | `backend/src/test/java/com/esoluzion/pricing/infrastructure/adapter/in/web/PricingApiIntegrationTest.java` |

**Goal:** Implementar tests de integración REST completos que verifiquen los 5 casos de prueba obligatorios y los 5 casos de error.

**Scope:**

**5 casos de éxito (Delta Spec §10.1):**
| Test | applicationDate | productId | brandId | priceList | price | Status |
|------|-----------------|-----------|---------|-----------|-------|--------|
| T1 | `2020-06-14T10:00:00Z` | 35455 | 1 | 1 | 35.50 | 200 |
| T2 | `2020-06-14T16:00:00Z` | 35455 | 1 | 2 | 25.45 | 200 |
| T3 | `2020-06-14T21:00:00Z` | 35455 | 1 | 1 | 35.50 | 200 |
| T4 | `2020-06-15T10:00:00Z` | 35455 | 1 | 3 | 30.50 | 200 |
| T5 | `2020-06-16T21:00:00Z` | 35455 | 1 | 4 | 38.95 | 200 |

Verificar que la respuesta incluye: `productId`, `brandId`, `priceList`, `startDate`, `endDate`, `price`, `currency`.

**5 casos de error (Delta Spec §10.2):**
| Test | Escenario | Status | Code |
|------|-----------|--------|------|
| E1 | `applicationDate` ausente | 400 | `VALIDATION_ERROR` |
| E2 | `productId` con valor no numérico (ej: "abc") | 400 | `VALIDATION_ERROR` |
| E3 | Producto inexistente (productId=99999) | 404 | `PRICE_NOT_FOUND` |
| E4 | Fecha sin tarifa (2019-01-01T00:00:00Z) | 404 | `PRICE_NOT_FOUND` |
| E5 | Formato de fecha inválido (ej: "14-06-2020") | 400 | `VALIDATION_ERROR` |

Verificar que la respuesta de error sigue el schema `ApiErrorResponse`: `timestamp`, `status`, `error`, `code`, `message`, `path`, `trace_id`, `details`.

**Out of scope:** Tests de performance, tests de seguridad, tests end-to-end con frontend real.

**Implementation notes:**
- Usar `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`.
- Usar `TestRestTemplate` o `MockMvc`. **Preferir `TestRestTemplate`** para tests de integración reales (ejercita serialización/deserialización real).
- Inyectar el puerto con `@LocalServerPort`.
- Cada test debe ser independiente y no depender del orden de ejecución.
- Verificar que los campos `timestamp`, `trace_id` existen y no son null (no verificar valores exactos porque son dinámicos).
- Para E1 (parámetro ausente), omitir completamente el query param `applicationDate`.
- Para E2 (valor no numérico), usar `productId=abc`.
- Para E5 (formato inválido), usar `applicationDate=14-06-2020` (formato dd-MM-yyyy en vez de ISO).

**Edge cases:**
- Asegurarse de que la base de datos H2 se reinicia con los datos seed para cada test (usar `@DirtiesContext` si es necesario, o confiar en que Flyway con `baseline-on-migrate` maneja esto en cada arranque del contexto).
- Si se usa `@Sql` para limpiar/reiniciar datos entre tests, verificar que no interfiere con Flyway.

**Done criteria:**
- Los 10 tests (5 éxito + 5 error) pasan.
- Cada test de éxito verifica los 7 campos del response.
- Cada test de error verifica los campos del `ApiErrorResponse`.
- No hay respuestas HTML ni Whitelabel Error Page en ningún test de error.
- `gradle test` pasa todos los tests.

**Verificación:**
```bash
cd backend && gradle test --tests "com.esoluzion.pricing.infrastructure.adapter.in.web.PricingApiIntegrationTest" --no-daemon 2>&1 | tail -15
```
```bash
# Verificar que los 10 tests específicos se ejecutan
cd backend && gradle test --no-daemon 2>&1 | grep -E "PricingApiIntegrationTest.*PASSED|PricingApiIntegrationTest.*FAILED"
```

**Handoff context:** Estos tests son la verificación principal de los criterios de aceptación AC1-AC5 y AC12-AC13.

---

## Tarea 14: Verificación de Cobertura JaCoCo

| Campo | Valor |
|-------|-------|
| **Agente** | test-architect |
| **Status** | todo |
| **Dependencias** | T11, T12, T13 |
| **Archivos a modificar** | Ninguno (la configuración está en `build.gradle.kts` desde T1) |
| **Archivos a verificar** | `backend/build/reports/jacoco/test/html/index.html` |

**Goal:** Verificar que la cobertura de código alcanza ≥ 85% con las exclusiones configuradas, y ajustar si es necesario.

**Scope:**
- Ejecutar `gradle test jacocoTestReport jacocoTestCoverageVerification`.
- Verificar que `jacocoTestCoverageVerification` pasa (umbral 0.85).
- Si la cobertura no alcanza 85%: (1) verificar que las exclusiones están funcionando correctamente, (2) identificar paquetes con baja cobertura, (3) añadir tests adicionales si es necesario.
- Generar reporte HTML para inspección visual.

**Out of scope:** Modificar las reglas de exclusión sin aprobación (están definidas en la spec).

**Implementation notes:**
- Las exclusiones configuradas en T1 deben excluir: `**/dto/**`, `**/entity/**`, `**/config/**`, `**/exceptions/**`, `**/exception/**`, `**/*MapperImpl*`, `**/*Application*`.
- La cobertura se mide sobre las clases no excluidas: domain, application, infrastructure adapters (no entities ni DTOs).
- Si la cobertura es < 85%, el build falla (`check` depende de `jacocoTestCoverageVerification`).
- Verificar que `gradle check` incluye la verificación de cobertura en el pipeline.

**Edge cases:**
- Las clases generadas por MapStruct (`*MapperImpl`) deben estar excluidas pero pueden no existir si MapStruct no se ejecutó. Verificar.
- Si `PricingApiApplication.java` no está en la lista de exclusiones, su cobertura es 0% (solo tiene método main). Asegurarse de que está excluido.
- Si los tests de integración no cubren suficiente código, añadir tests de borde adicionales.

**Done criteria:**
- `gradle jacocoTestCoverageVerification` pasa con éxito.
- `gradle check` pasa con éxito.
- El reporte HTML muestra ≥ 85% de cobertura de instrucciones en las clases no excluidas.

**Verificación:**
```bash
cd backend && gradle clean test jacocoTestReport jacocoTestCoverageVerification --no-daemon 2>&1 | tail -20
```
```bash
# Verificar el reporte HTML
ls -la backend/build/reports/jacoco/test/html/index.html
```
```bash
# Verificar la cobertura global (alternativa)
cd backend && gradle jacocoTestReport --no-daemon 2>&1 | grep -i "coverage\|jacoco"
```

**Handoff context:** Esta tarea es el quality gate final. Si falla, se necesita añadir más tests (coordinar con test-architect).

---

## Tarea 15: Docker Compose + Dockerfiles

| Campo | Valor |
|-------|-------|
| **Agente** | devops-architect |
| **Status** | todo |
| **Dependencias** | T1 |
| **Archivos a crear** | `infra/docker-compose.yml`, `infra/Dockerfile.backend`, `infra/Dockerfile.frontend`, `infra/.env` |

**Goal:** Crear la infraestructura Docker Compose para orquestar backend y frontend placeholder.

**Scope:**

1. **`docker-compose.yml`** — 2 servicios:
   - `pricing-api`: build context `..` (raíz del proyecto), dockerfile `infra/Dockerfile.backend`, puerto `${BACKEND_PORT:-8080}:8080`, healthcheck con curl al endpoint de precios, red `esoluzion-network`.
   - `pricing-console`: build context `../frontend`, dockerfile `../infra/Dockerfile.frontend`, puerto `${FRONTEND_PORT:-3000}:80`, depends_on pricing-api, red `esoluzion-network`.
   - Red `esoluzion-network` bridge.

2. **`Dockerfile.backend`** — Multi-stage:
   - Stage 1 (build): `gradle:8-jdk17`, copiar build.gradle.kts, settings.gradle.kts, gradlew, gradle/, src/, docs/. Ejecutar `gradle build --no-daemon -x test`.
   - Stage 2 (runtime): `eclipse-temurin:17-jre`, copiar JAR desde stage 1, EXPOSE 8080, ENTRYPOINT `java -jar app.jar`.

3. **`Dockerfile.frontend`** — Placeholder multi-stage:
   - Stage 1 (build): `node:18-alpine`, copiar package.json, ejecutar `npm install && npm run build`.
   - Stage 2 (runtime): `nginx:alpine`, copiar build output a `/usr/share/nginx/html`, EXPOSE 80.

4. **`.env`** — Variables de entorno:
   - `BACKEND_PORT=8080`
   - `FRONTEND_PORT=3000`

**Out of scope:** Docker Compose con dependencias externas (no hay), profiles de Spring, volúmenes persistentes.

**Implementation notes:**
- El context de build para `pricing-api` debe ser `..` (raíz del proyecto) porque el Dockerfile está en `infra/Dockerfile.backend` y necesita acceso a `backend/build/libs/*.jar` y `docs/api/pricing-api.yaml`.
- El `COPY ../docs ./docs` en Dockerfile.backend funciona cuando el build context es la raíz del proyecto (no `backend/`).
- El healthcheck usa curl: `curl -f http://localhost:8080/api/prices?applicationDate=2020-06-14T10:00:00Z&productId=35455&brandId=1`.
- El JAR generado por Spring Boot se nombra según `rootProject.name` en settings.gradle.kts (T1). El nombre exacto será `pricing-api-*.jar` o similar. Ajustar el `COPY --from=build /app/build/libs/*.jar app.jar` en consecuencia.
- Para Dockerfile.frontend, verificar que `frontend/package.json` existe (se creará en incremento 002). Para este incremento, puede ser un placeholder mínimo.

**Edge cases:**
- Si el build context no es la raíz, `COPY ../docs ./docs` falla porque `../docs` está fuera del contexto. Debe usarse `context: ..` en docker-compose.yml.
- El nombre del JAR puede variar. Usar `build/libs/*.jar` con wildcard.
- El frontend placeholder puede fallar si no hay `package.json`. Para este incremento, docker-compose debe poder construirse aunque el frontend falle opcionalmente, o crear un `package.json` mínimo.

**Done criteria:**
- `docker-compose.yml` define 2 servicios con la red y healthcheck.
- `Dockerfile.backend` multi-stage compila y ejecuta la aplicación.
- `Dockerfile.frontend` placeholder existe para el servicio frontend.
- `.env` contiene las variables de puerto.

**Verificación:**
```bash
# Validar sintaxis de docker-compose
docker-compose -f infra/docker-compose.yml config 2>&1 | head -5
```
```bash
# Verificar que los archivos existen
ls -la infra/docker-compose.yml infra/Dockerfile.backend infra/Dockerfile.frontend infra/.env
```

**Handoff context:** La infraestructura Docker permite levantar el stack completo con `docker-compose up`. El frontend es un placeholder hasta el incremento 002.

---

## Tarea 16: README.md

| Campo | Valor |
|-------|-------|
| **Agente** | git-executor |
| **Status** | todo |
| **Dependencias** | Ninguna |
| **Archivos a crear** | `README.md` |

**Goal:** Crear el README principal del repositorio con información del proyecto, instrucciones de ejecución y referencia a la documentación.

**Scope:**
- Título: "Pricing API — Sistema de Consulta de Tarifas".
- Descripción breve del proyecto.
- Stack tecnológico (Java 21+, Spring Boot 3.x, Gradle, H2, Flyway, JPA, Arquitectura Hexagonal, OpenAPI, Docker).
- Estructura del proyecto (monorepo con backend/, frontend/, infra/, docs/).
- Requisitos previos (JDK 17+, Gradle 8+, Docker).
- Instrucciones para ejecutar localmente:
  - `cd backend && gradle bootRun`
  - `curl http://localhost:8080/api/prices?...`
- Instrucciones para ejecutar con Docker:
  - `docker-compose -f infra/docker-compose.yml up`
- Endpoint de ejemplo.
- Ejecutar tests: `cd backend && gradle test`.
- Referencia a la documentación (docs/api/pricing-api.yaml, docs/specs/, docs/architecture/).
- Nota sobre el frontend (incremento 002).

**Out of scope:** Documentación exhaustiva de arquitectura (ya está en docs/), guías de contribución detalladas, badges de CI/CD.

**Implementation notes:**
- Usar formato Markdown estándar.
- Incluir bloques de código con sintaxis resaltada (bash, json).
- Incluir la respuesta de ejemplo del endpoint.
- Mantener conciso pero informativo — es una prueba técnica.

**Edge cases:** Ninguno significativo.

**Done criteria:**
- README.md cubre todos los puntos del scope.
- Las instrucciones de ejecución son correctas y verificables.
- Incluye referencias a la documentación relevante.

**Verificación:**
```bash
wc -l README.md
```
```bash
# Verificar secciones clave
grep -E "## (Descripción|Stack|Estructura|Requisitos|Ejecución|Tests|Documentación)" README.md
```

**Handoff context:** El README es la puerta de entrada para cualquier desarrollador que tome el proyecto.

---

## Cross-Task Verification Matrix

| Criterio de Aceptación (Delta Spec §12) | Tareas que lo verifican |
|------------------------------------------|-------------------------|
| AC1: 5 casos T1-T5 retornan 200 | T13 |
| AC2: 404 con ApiErrorResponse | T13 |
| AC3: 400 con ApiErrorResponse | T13 |
| AC4: Response incluye todos los campos | T13 |
| AC5: Priority resuelve correctamente | T13 (T2 verifica priority DESC) |
| AC6: Dominio sin dependencias de frameworks | T4 |
| AC7: Aplicación sin dependencias de infraestructura | T5, T6 |
| AC8: Entidades JPA no expuestas fuera de infra | T7 |
| AC9: OpenAPI coincide con implementación | T2 |
| AC10: JaCoCo ≥ 85% | T14 |
| AC11: 5 casos de prueba pasan | T13 |
| AC12: ApiErrorResponse campos obligatorios | T9, T13 |
| AC13: Sin Whitelabel Error Page | T9, T13 |
| AC14: Docker Compose funcional | T15 |

---

## Forbidden Stale Terms (Recordatorio para Executor)

Los siguientes términos están **prohibidos** en código, archivos y commits. Ver Delta Spec §13.4:

| Término prohibido | Usar en su lugar |
|-------------------|-----------------|
| `PriceDto` | `PriceResponse` (API) o `PriceResult` (application) |
| `PriceService` | `GetApplicablePriceUseCase` o `GetApplicablePriceService` |
| `Tarifa` | Usar nombres en inglés |
| `Manager`, `Processor`, `Handler` (como sufijo de dominio) | Nombres de negocio |

---

## Executor Notes

> *(A completar por Executor durante la implementación)*
