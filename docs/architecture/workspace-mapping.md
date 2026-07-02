# Workspace Mapping — eSoluzion Pricing System

> **Estado:** Accepted  
> **Última revisión:** 2026-07-02  
> **Owner:** Enterprise Architect  
> **Referencia:** [ADR-001: Estructura del Proyecto](./decision-records/ADR-001-project-structure.md)

---

## 1. Tipo de Workspace

**Monorepo** (repositorio único con módulos internos).

> **Justificación:** Ver [ADR-001](./decision-records/ADR-001-project-structure.md). Al ser una prueba técnica con un único desarrollador y un alcance limitado, no se usa la estructura `projects/` de solution workspace multi-repo. Los módulos viven directamente en la raíz del repositorio.

---

## 2. Mapeo de Módulos

| Módulo | Ruta Relativa | Tipo | Bounded Context | Tecnología | Owner | Estado |
|--------|---------------|------|-----------------|------------|-------|--------|
| **Backend** | `backend/` | Servicio REST | Pricing | Java 21+, Spring Boot 3.x, Gradle (Kotlin DSL), JPA, H2 | Equipo de desarrollo | Pendiente de implementación |
| **Frontend** | `frontend/` | SPA | Pricing (consumer) | React 18+ (minimalista, Vite) | Equipo de desarrollo | Pendiente de implementación |
| **Infra** | `infra/` | Orquestación | N/A (transversal) | Docker Compose | Equipo de desarrollo | Pendiente de implementación |
| **Docs - API** | `docs/api/` | Contrato | Pricing | OpenAPI 3.0 (YAML) | Equipo de desarrollo | Pendiente de creación |
| **Docs - Architecture** | `docs/architecture/` | Arquitectura | N/A (enterprise) | Markdown | Enterprise Architect | **Activo** (este documento) |

---

## 3. Estructura de Directorios Completa

```
eSoluzion-prueba/                          # Raíz del repositorio (workspace)
│
├── backend/                               # Módulo: Servicio REST
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                      # Código Java (Arquitectura Hexagonal)
│   │   │   │   └── com/esoluzion/pricing/
│   │   │   │       ├── domain/            # Capa de dominio (entidades, VOs, puertos)
│   │   │   │       ├── application/       # Capa de aplicación (casos de uso)
│   │   │   │       └── infrastructure/    # Capa de infraestructura (adapters)
│   │   │   └── resources/
│   │   │       ├── application.yml        # Configuración Spring Boot
│   │   │       └── db/
│   │   │           └── migration/         # Migraciones Flyway
│   │   │               ├── V1__create_prices_table.sql
│   │   │               └── V2__seed_prices_data.sql
│   │   └── test/
│   │       └── java/                      # Tests de integración REST
│   ├── build.gradle.kts                   # Build script (Kotlin DSL)
│   ├── settings.gradle.kts
│   └── gradle/
│
├── frontend/                              # Módulo: SPA React minimalista
│   ├── public/                            # Assets estáticos
│   ├── src/
│   │   ├── components/                    # Componentes React
│   │   ├── App.jsx                        # Componente raíz
│   │   ├── main.jsx                       # Entry point
│   │   └── App.css                        # Estilos globales
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js                     # Build tool (Vite)
│   └── Dockerfile
│
├── infra/                                 # Módulo: Infraestructura
│   ├── docker-compose.yml                 # Orquestación de servicios
│   ├── Dockerfile.backend                 # Imagen Docker del backend
│   ├── Dockerfile.frontend                # Imagen Docker del frontend
│   └── .env                               # Variables de entorno (puertos, etc.)
│
├── docs/
│   ├── api/                               # Contrato OpenAPI (fuente de verdad)
│   │   └── pricing-api.yaml              # OpenAPI 3.0 spec
│   ├── TestJava2024_1.txt                 # Requisitos originales de la prueba
│   └── architecture/                      # Artefactos de arquitectura enterprise
│       ├── system-landscape.md            # C4 Level 1 y 2
│       ├── context-map.md                 # Bounded Contexts y lenguaje ubicuo
│       ├── integration-map.md             # Contratos de integración
│       ├── workspace-mapping.md           # Este documento
│       └── decision-records/
│           └── ADR-001-project-structure.md
│
└── README.md                              # README del repositorio
```

---

## 4. Rutas Absolutas de Artefactos Enterprise

| Artefacto | Ruta Absoluta | Estado |
|-----------|---------------|--------|
| System Landscape | `docs/architecture/system-landscape.md` | ✅ Creado |
| Context Map | `docs/architecture/context-map.md` | ✅ Creado |
| Integration Map | `docs/architecture/integration-map.md` | ✅ Creado |
| ADR-001 Project Structure | `docs/architecture/decision-records/ADR-001-project-structure.md` | ✅ Creado |
| Workspace Mapping | `docs/architecture/workspace-mapping.md` | ✅ Creado (este documento) |

---

## 5. Convenciones de Nomenclatura

| Convención | Regla |
|------------|-------|
| Carpetas de módulos | Minúsculas, sin guiones (ej: `backend/`, `frontend/`) |
| Paquetes Java | `com.esoluzion.pricing.{capa}` siguiendo arquitectura hexagonal |
| Archivos de configuración | `application.yml` (Spring Boot), `.env` (Docker) |
| Contratos API | `pricing-api.yaml` en `docs/api/` |
| ADRs | `ADR-{NNN}-{kebab-case-title}.md` en `docs/architecture/decision-records/` |
| Documentación de arquitectura | Markdown en `docs/architecture/` |

---

## 6. Dependencias entre Módulos

```
frontend/ ──────► docs/api/pricing-api.yaml (contrato)
     │
     └─────────► backend/ (HTTP REST via Docker network)

backend/  ──────► docs/api/pricing-api.yaml (contrato)
     │
     └─────────► H2 embebida (interno)

infra/    ──────► backend/ (Dockerfile.backend)
     │
     └─────────► frontend/ (Dockerfile.frontend)
```

---

## 7. Configuración de Docker Compose

| Servicio | Imagen / Build | Puerto Host | Puerto Container | Red |
|----------|---------------|-------------|------------------|-----|
| `pricing-api` | `infra/Dockerfile.backend` → `backend/` | 8080 | 8080 | esoluzion-network |
| `pricing-console` | `infra/Dockerfile.frontend` → `frontend/` (multi-stage: Stage 1 = Node.js build con `npm install && npm run build`; Stage 2 = nginx sirviendo desde `/usr/share/nginx/html`) | 3000 | 80 (nginx) | esoluzion-network |

---

## 8. Referencias

- [System Landscape](./system-landscape.md)
- [Context Map](./context-map.md)
- [Integration Map](./integration-map.md)
- [ADR-001: Estructura del Proyecto](./decision-records/ADR-001-project-structure.md)
