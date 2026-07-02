# ADR-004: Generación de Interfaces REST con OpenAPI Generator

> **Estado:** Accepted  
> **Fecha:** 2026-07-02  
> **Owner:** Planner  
> **Decisión:** Usar OpenAPI Generator con `interfaceOnly: true` para generar interfaces de controller y DTOs de API.

## Contexto

Inicialmente se planificaron controllers REST manuales (D11 original en shared context). Sin embargo, se identificó que usar OpenAPI Generator garantiza que el contrato OpenAPI sea la fuente de verdad única, eliminando el riesgo de drift entre la especificación y la implementación.

Para un proyecto con un contrato API bien definido (incluso con un solo endpoint), la generación de interfaces asegura que:
- Los DTOs de respuesta y error siempre coincidan con el schema OpenAPI.
- Las firmas de los métodos del controller reflejen exactamente los parámetros y tipos definidos en el contrato.
- Cualquier cambio en el OpenAPI se refleje inmediatamente en el código compilable.

## Decisión

Usar el plugin `org.openapi.generator` versión 7.6.0 con configuración `spring` y las siguientes opciones:

| Opción | Valor | Justificación |
|--------|-------|---------------|
| `interfaceOnly` | `true` | Solo genera interfaces, no implementaciones. El controller las implementa manualmente. |
| `useSpringBoot3` | `true` | Compatible con Spring Boot 3.x (Jakarta namespace). |
| `useJakartaEe` | `true` | Usa `jakarta.*` en vez de `javax.*`. Alineado con Spring Boot 3. |
| `dateLibrary` | `java8-localdatetime` | Genera campos `LocalDateTime` alineados con ADR-003. |
| `skipDefaultInterface` | `true` | No genera métodos default en las interfaces. |
| `useTags` | `true` | Usa los tags de OpenAPI para nombrar las interfaces generadas. |
| `generateBuilders` | `true` | Genera builder pattern para DTOs. |
| `library` | `spring-boot` | Target library Spring Boot. |

**Paquetes generados:**
- Interfaces API: `com.esoluzion.pricing.infrastructure.adapter.in.web.api`
- DTOs: `com.esoluzion.pricing.infrastructure.adapter.in.web.dto`
- Invoker: `com.esoluzion.pricing.infrastructure.adapter.in.web`

**Artefactos generados principales:**
- `PriceApi` — Interface del controller con método `getApplicablePrice()`.
- `PriceResponse` — DTO de respuesta (schema `PriceResponse` del OpenAPI).
- `ApiErrorResponse` — DTO de error (schema `ApiErrorResponse` del OpenAPI).
- `ApiErrorDetail` — DTO de detalle de error (schema `ApiErrorDetail` del OpenAPI).

El controller (`PriceController`) implementa la interfaz generada `PriceApi` y provee la lógica de delegación al caso de uso.

## Alternativas Consideradas

1. **Controllers manuales (decisión original)** — Simple pero riesgo de drift con OpenAPI. Los DTOs deben mantenerse manualmente en sincronía con el schema.
2. **OpenAPI Generator completo (sin interfaceOnly)** — Genera implementaciones completas de controllers con anotaciones. Demasiado verbose y quita control sobre la lógica.
3. **OpenAPI Generator interfaceOnly** — Balance óptimo entre sincronización automática del contrato y control sobre la implementación. (**SELECCIONADA**)

## Consecuencias

### Positivas
- El contrato OpenAPI es la fuente de verdad única; DTOs e interfaces siempre sincronizados.
- Elimina código boilerplate de DTOs (getters, setters, builders, validaciones de schema).
- Si el OpenAPI cambia, el compilador falla si el controller no se actualiza (type-safety).
- Los DTOs de error (`ApiErrorResponse`, `ApiErrorDetail`) se generan consistentes con el schema.

### Negativas
- Dependencia adicional del plugin `org.openapi.generator` en el build.
- Paso adicional en el ciclo de compilación (`openApiGenerate` antes de `compileJava`).
- Los DTOs generados no se pueden modificar manualmente (cualquier cambio manual se pierde al regenerar).
- Los DTOs generados pueden incluir campos o anotaciones que no se alinean perfectamente con los estándares de error (requiere verificación post-generación).

## Criterios de Revisión

- Si el build supera los 30s por la generación, evaluar caché del plugin o generated sources caching.
- Si los DTOs generados no se alinean con los estándares de error (`springboot-java-rest-error-response-standards`), ajustar el schema OpenAPI o la configuración del generador.
- Si la interfaz generada impone restricciones no deseadas (ej: parámetros no opcionales), evaluar `skipDefaultInterface` o ajustes en el OpenAPI.

## Referencias

- Delta Spec: `docs/specs/increment-001-pricing-api.md` §9.1 (configuración Gradle)
- OpenAPI Contract: `docs/api/pricing-api.yaml`
- Shared Context D11: `docs/specs/.working/pricing-sdd-context.md`
