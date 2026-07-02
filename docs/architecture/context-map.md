# Context Map — eSoluzion Pricing System

> **Estado:** Accepted  
> **Última revisión:** 2026-07-02  
> **Owner:** Enterprise Architect  
> **Referencia:** [System Landscape](./system-landscape.md)

---

## 1. Resumen de Bounded Contexts

Para este sistema, dado que es un servicio único autocontenido, existe **un solo Bounded Context**:

| Bounded Context | Descripción | Owner | Sistema que lo implementa |
|-----------------|-------------|-------|--------------------------|
| **Pricing** | Gestiona la lógica de tarifas/precios de productos para cadenas comerciales, incluyendo vigencia temporal y reglas de prioridad. | Equipo de desarrollo | Pricing API (Spring Boot) |

> **Justificación:** Al ser un sistema monolítico de prueba técnica con un único dominio funcional (consulta de precios), no es necesario dividir en múltiples bounded contexts. La Arquitectura Hexagonal interna garantiza la separación de responsabilidades dentro del contexto.

---

## 2. Lenguaje Ubicuo — Bounded Context: Pricing

### 2.1 Entidades del Dominio

| Término | Definición | Representación técnica |
|---------|-----------|----------------------|
| **Brand** (Cadena) | Grupo comercial al que pertenece el producto. Ejemplo: ZARA (ID=1). | `brandId: Long` |
| **Product** (Producto) | Artículo comercial identificable de forma única dentro del catálogo. | `productId: Long` |
| **PriceList** (Tarifa) | Identificador de una tarifa de precios específica. Una tarifa agrupa condiciones de precio para un producto en un rango temporal. | `priceList: Long` |
| **Price** (Precio) | Valor monetario final de venta de un producto bajo una tarifa específica. | `price: BigDecimal` |
| **Currency** (Moneda) | Código ISO de la moneda en que se expresa el precio. | `curr: String` (ej: "EUR") |
| **Priority** (Prioridad) | Valor numérico que determina qué tarifa prevalece cuando múltiples tarifas son vigentes para la misma fecha. Mayor valor = mayor prioridad. | `priority: Integer` |
| **ApplicationDate** (Fecha de Aplicación) | Fecha y hora para la cual se consulta el precio aplicable. | `applicationDate: LocalDateTime` |
| **StartDate** (Fecha Inicio) | Inicio del rango de vigencia de una tarifa. | `startDate: LocalDateTime` |
| **EndDate** (Fecha Fin) | Fin del rango de vigencia de una tarifa. | `endDate: LocalDateTime` |

### 2.2 Value Objects

| Value Object | Descripción |
|--------------|-------------|
| **PriceQuery** | Objeto de consulta que encapsula los parámetros de entrada: applicationDate, productId, brandId. |
| **ApplicablePrice** | Resultado de la consulta: productId, brandId, priceList, startDate, endDate, price. |
| **DateRange** | Rango temporal definido por startDate y endDate. |

### 2.3 Servicios de Dominio

| Servicio | Responsabilidad |
|----------|-----------------|
| **PriceResolver** (o PricingService) | Dado un PriceQuery, encuentra todas las tarifas vigentes y selecciona la de mayor prioridad. |
| **PriceRepository** (Puerto) | Interfaz para consultar tarifas desde el almacén de datos. |

### 2.4 Reglas de Negocio

| # | Regla |
|---|-------|
| R1 | Una tarifa es **vigente** si `startDate <= applicationDate <= endDate`. |
| R2 | Si múltiples tarifas son vigentes para un mismo producto/cadena/fecha, se aplica la de **mayor PRIORITY**. |
| R3 | Si no hay tarifas vigentes, se retorna un error (404 o similar). |
| R4 | El precio retornado es el `PRICE` de la tarifa seleccionada. |
| R5 | La moneda (`CURR`) se retorna como parte del resultado pero no se convierte. |

### 2.5 Términos que NO deben reutilizarse fuera del contexto

| Término | Razón |
|---------|-------|
| `PriceList` | Específico del dominio de pricing. No debe usarse para otros conceptos de "lista" en otros contextos. |
| `Priority` | La semántica de "desambiguador de tarifas" es exclusiva de este contexto. |
| `Brand` | En este contexto significa "cadena comercial". En otros contextos podría significar "marca de producto" (concepto diferente). |

---

## 3. Context Map — Relaciones DDD

Al ser un sistema de un solo bounded context, las relaciones son **internas**:

```
┌─────────────────────────────────────────────────────┐
│              BOUNDED CONTEXT: Pricing                │
│                                                     │
│  ┌─────────────┐    ┌──────────────┐               │
│  │   Domain     │    │  Application │               │
│  │  (Entities,  │◄──►│  (Use Cases, │               │
│  │   VOs)       │    │   Ports)     │               │
│  └─────────────┘    └──────┬───────┘               │
│                            │                        │
│                     ┌──────▼───────┐               │
│                     │  Adapter     │               │
│                     │  (H2 via JPA)│               │
│                     └──────────────┘               │
│                                                     │
└─────────────────────────────────────────────────────┘
         ▲                                ▲
         │ REST (OpenAPI)                 │ HTTP
         │                                │
    ┌────┴───────┐                 ┌──────┴───────┐
    │  External   │                 │   Pricing    │
    │  Consumers  │                 │   Console    │
    │  (Tests,    │                 │   (Frontend) │
    │   Postman)  │                 │              │
    └────────────┘                 └──────────────┘
```

### 3.1 Relaciones

| Relación | Tipo DDD | Upstream | Downstream | Descripción |
|----------|----------|----------|------------|-------------|
| Pricing API → H2 DB | **Published Language** (via JPA entities) | Pricing (Domain) | H2 (Infrastructure) | El dominio define la interfaz del repository; el adapter JPA implementa contra H2. |
| Frontend → Pricing API | **Open Host Service** + **Published Language** (OpenAPI) | Pricing API | Frontend | El backend expone un contrato OpenAPI estable. El frontend se adapta al contrato. |
| Tests → Pricing API | **Conformist** | Pricing API | Tests de integración | Los tests consumen el endpoint tal como está definido. No negocian el contrato. |

---

## 4. Fuente de Verdad de Datos

| Dato | Fuente de Verdad | Sistema Owner |
|------|------------------|---------------|
| Tabla PRICES (tarifas) | H2 Database (embebida en Pricing API) | Pricing API |
| Contrato de API | OpenAPI spec (`docs/api/`) | Pricing API |
| Datos seed | Flyway V2 migration (V2__seed_prices_data.sql) | Pricing API |

---

## 5. Evolución Futura (fuera de alcance actual)

Si el sistema creciera, los siguientes bounded contexts podrían emerger:

| Bounded Context Potencial | Trigger |
|---------------------------|---------|
| **Catalog** | Si se necesita gestionar productos, categorías, descripciones. |
| **Promotion** | Si las tarifas se combinan con promociones, cupones o descuentos. |
| **Currency Exchange** | Si se requiere conversión de monedas en tiempo real. |
| **Inventory** | Si se necesita validar stock antes de mostrar precio. |

> **Nota:** Estos contextos son especulativos y están fuera del alcance de la prueba técnica actual. Se documentan para mostrar la capacidad de evolución del diseño.

---

## 6. Referencias

- [System Landscape](./system-landscape.md)
- [Integration Map](./integration-map.md)
- [ADR-001: Estructura del Proyecto](./decision-records/ADR-001-project-structure.md)
