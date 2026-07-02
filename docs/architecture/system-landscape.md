# System Landscape — eSoluzion Pricing System

> **Estado:** Accepted  
> **Última revisión:** 2026-07-02  
> **Owner:** Enterprise Architect  
> **Nivel C4:** Level 1 (System Context) + Level 2 (Containers)

---

## 1. Propósito del Sistema

Sistema de consulta de tarifas/precios de productos para una cadena de comercio electrónico. Permite determinar el precio aplicable a un producto de una cadena comercial en una fecha y hora específicas, aplicando reglas de prioridad y vigencia temporal.

---

## 2. Modelo C4 — Level 1: System Context

### 2.1 Actores (Usuarios)

| Actor | Descripción | Interacción principal |
|-------|-------------|----------------------|
| **Desarrollador / QA** | Profesional técnico que consume el endpoint REST para validar la lógica de precios. | Invoca el endpoint `/api/prices` via HTTP o a través de la interfaz web del frontend. |

> **Nota:** No hay usuarios finales de negocio en este alcance. El sistema es una prueba técnica que demuestra la capacidad de resolución de un dominio de pricing con reglas de prioridad y vigencia temporal.

### 2.2 Sistemas

| Sistema | Tipo | Responsabilidad | Ownership |
|---------|------|-----------------|-----------|
| **Pricing API (Backend)** | Servicio REST (Spring Boot 3.x) | Expone endpoint de consulta de precios. Contiene la lógica de dominio para resolver tarifas por fecha, producto y cadena. | Equipo de desarrollo (único) |
| **Pricing Console (Frontend)** | SPA (React 18+) | Interfaz minimalista construida con React 18+ para probar el endpoint de forma visual. Permite ingresar fecha, productId y brandId. | Equipo de desarrollo (único) |
| **H2 Database (Embebida)** | Base de datos en memoria | Almacena la tabla PRICES con las tarifas. Se inicializa con datos seed al arrancar el backend. | Backend (interno) |

### 2.3 Dependencias Externas

| Dependencia | Tipo | Justificación |
|-------------|------|---------------|
| **Ninguna** | — | El sistema es autocontenido. No consume APIs externas, no se conecta a brokers, no depende de servicios de terceros. |

### 2.4 Diagrama de Contexto (texto)

```
┌─────────────────────────────────────────────────────────┐
│                   ACTOR: Desarrollador                   │
│              (consume API / usa Frontend)                │
└──────────────┬──────────────────────┬───────────────────┘
               │ HTTP/REST            │ HTTP (Browser)
               ▼                      ▼
┌──────────────────────┐   ┌──────────────────────┐
│   Pricing API        │   │   Pricing Console     │
│   (Spring Boot)      │◄──│   (SPA)               │
│                      │   │                       │
│  ┌────────────────┐  │   └───────────────────────┘
│  │  H2 Database   │  │
│  │  (embebida)    │  │
│  └────────────────┘  │
└──────────────────────┘
```

---

## 3. Modelo C4 — Level 2: Containers

| Container | Tecnología | Responsabilidad | Puerto |
|-----------|-----------|-----------------|--------|
| **pricing-api** | Java 21+, Spring Boot 3.x, Gradle (Kotlin DSL) | Servicio REST con Arquitectura Hexagonal. Expone endpoint de consulta de precios. | 8080 |
| **pricing-console** | React 18+ (SPA minimalista, Vite) | Interfaz web para invocar el endpoint de forma visual. | 3000 (nginx) |
| **h2-db** | H2 Database (embebida en JVM) | Almacén en memoria de la tabla PRICES. Se inicializa con migraciones Flyway. | N/A (embebida) |
| **docker-compose** | Docker Compose | Orquesta los containers de backend y frontend en una red aislada. | N/A |

---

## 4. Ownership

| Elemento | Owner Funcional | Owner Técnico |
|----------|-----------------|---------------|
| Pricing API | Equipo de desarrollo | Equipo de desarrollo |
| Pricing Console | Equipo de desarrollo | Equipo de desarrollo |
| H2 Database | Equipo de desarrollo | Equipo de desarrollo |
| Docker Compose | Equipo de desarrollo | Equipo de desarrollo |
| OpenAPI Spec | Equipo de desarrollo | Equipo de desarrollo |

> **Nota:** Al ser un proyecto de prueba técnica (single-developer), todos los ownerships recaen en el mismo desarrollador. En un escenario productivo, cada bounded context tendría owners diferenciados.

---

## 5. Flujos Principales

### 5.1 Consulta de Precio Aplicable

```
Desarrollador → Frontend → Pricing API → H2 DB
                Pricing API ← H2 DB
                Frontend ← Pricing API
Desarrollador ← Frontend
```

1. El desarrollador ingresa fecha, productId y brandId en el frontend.
2. El frontend invoca `GET /api/prices?applicationDate=...&productId=...&brandId=...`.
3. El backend consulta la tabla PRICES filtrando por productId, brandId y rango de fechas.
4. Si hay múltiples tarifas vigentes, aplica la de mayor PRIORITY.
5. Retorna el precio aplicable con los datos de la tarifa.

---

## 6. Restricciones y Suposiciones

| # | Restricción / Suposición |
|---|--------------------------|
| 1 | El sistema opera en una sola zona horaria (UTC o la del servidor). No se requiere conversión de zonas horarias. |
| 2 | La base de datos H2 es efímera: se reinicia con cada arranque del backend. No hay persistencia entre reinicios. |
| 3 | Los datos seed son estáticos y se cargan desde migraciones Flyway (V2) |
| 4 | No se requiere autenticación ni autorización (no hay Keycloak ni JWT en este alcance). |
| 5 | No se requiere observabilidad distribuida (no hay traces distribuidos en este alcance). |
| 6 | El frontend es una herramienta de demostración, no una aplicación de producción. |
| 7 | La moneda es siempre EUR en los datos seed, pero el campo CURR se modela como variable. |
| 8 | La cobertura de tests debe ser ≥ 85% en todas las clases testeables del backend (excluyendo DTOs, configuraciones, entidades JPA y mappers MapStruct según estándar JaCoCo). |

---

## 7. Riesgos Identificados

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| H2 en memoria no persiste datos | Bajo (es una prueba técnica) | Documentado como restricción. Aceptable para el alcance. |
| Sin autenticación | Medio (si se expusiera en producción) | Aceptable para prueba técnica. En producción se requeriría Keycloak. |
| Single point of failure (un solo backend) | Bajo (prueba técnica) | Aceptable. En producción se requeriría replicación. |

---

## 8. Referencias

- [Requisitos funcionales](../TestJava2024_1.txt)
- [Context Map](./context-map.md)
- [Integration Map](./integration-map.md)
- [ADR-001: Estructura del Proyecto](./decision-records/ADR-001-project-structure.md)
- [Workspace Mapping](./workspace-mapping.md)
