# ADR-001: Estructura del Proyecto — Monorepo

> **Estado:** Accepted  
> **Fecha:** 2026-07-02  
> **Owner:** Enterprise Architect  
> **Decisión:** Estructura monorepo con módulos internos (backend, frontend, infra)

---

## 1. Contexto

Se necesita definir la estructura de repositorios para el sistema eSoluzion Pricing, compuesto por:
- Un backend Spring Boot (servicio REST con Arquitectura Hexagonal)
- Un frontend SPA con React 18+ y Vite (interfaz de prueba minimalista)
- Infraestructura Docker Compose (multi-stage build para frontend con nginx)
- Documentación de arquitectura y API

El sistema es una **prueba técnica** de alcance limitado, con un único equipo/desarrollador.

> **Nota sobre calidad:** Existe un quality gate obligatorio de ≥ 85% de cobertura de tests en el backend (JaCoCo), excluyendo DTOs, configuraciones, entidades JPA y mappers MapStruct.

---

## 2. Decisión

Se adopta una **estructura monorepo** (un solo repositorio Git) con módulos internos organizados por responsabilidad:

```
eSoluzion-prueba/
├── backend/              # Spring Boot + Hexagonal Architecture
│   ├── src/
│   ├── build.gradle.kts
│   └── ...
├── frontend/             # SPA React 18+ (Vite)
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── App.css
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── infra/                # Docker Compose + configs
│   ├── docker-compose.yml
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── ...
├── docs/
│   ├── api/              # OpenAPI spec (fuente de verdad del contrato)
│   │   └── pricing-api.yaml
│   ├── TestJava2024_1.txt # Requisitos originales
│   └── architecture/     # Artefactos de arquitectura enterprise
│       ├── system-landscape.md
│       ├── context-map.md
│       ├── integration-map.md
│       ├── workspace-mapping.md
│       └── decision-records/
│           └── ADR-001-project-structure.md
└── README.md
```

---

## 3. Alternativas Consideradas

### 3.1 Multirepo (un repositorio por componente)

```
repos/
├── pricing-api/          # Repo independiente
├── pricing-console/      # Repo independiente
├── pricing-infra/        # Repo independiente
└── pricing-docs/         # Repo independiente
```

**Ventajas:**
- Independencia de versionado y deploy por componente.
- Permisos granulares por repositorio.
- Escala mejor para equipos grandes y distribuidos.

**Desventajas:**
- Complejidad de coordinación entre repos (versiones compatibles, contratos).
- Overhead de gestión de múltiples repos para un proyecto pequeño.
- Difícil mantener documentación de arquitectura sincronizada con el código.
- No justificado para un proyecto de prueba técnica con un solo desarrollador.

**Conclusión:** Rechazada. El costo de coordinación supera los beneficios para este alcance.

### 3.2 Monorepo sin separación de módulos

```
eSoluzion-prueba/
├── src/                  # Todo el código mezclado
├── resources/
└── ...
```

**Ventajas:**
- Simplicidad máxima.

**Desventajas:**
- No hay separación de responsabilidades.
- Imposible dockerizar frontend y backend de forma independiente.
- Mezcla de tecnologías (Java + JS) en el mismo árbol de fuentes.
- Dificulta la evolución futura.

**Conclusión:** Rechazada. La separación por módulos es mínima y aporta claridad.

### 3.3 Monorepo con módulos internos (DECISIÓN ADOPTADA)

**Ventajas:**
- Un solo repositorio, un solo `git clone`, un solo lugar para buscar.
- Separación clara de responsabilidades (backend, frontend, infra, docs).
- Docker Compose puede referenciar los módulos directamente con rutas relativas.
- La documentación de arquitectura vive junto al código que documenta.
- El contrato OpenAPI está accesible para ambos módulos (backend y frontend).
- Ideal para pruebas técnicas donde el revisor quiere ver todo el sistema de un vistazo.
- Bajo overhead de gestión.

**Desventajas:**
- Si el proyecto crece a múltiples equipos, podría necesitar migración a multirepo.
- CI/CD debe ser selectivo (solo reconstruir lo que cambió).

**Conclusión:** Aceptada. El beneficio de simplicidad y coherencia supera las desventajas para este alcance.

---

## 4. Consecuencias

### Positivas
- **Simplicidad operativa:** Un solo repositorio que clonar, un solo lugar para documentar.
- **Coherencia:** La documentación de arquitectura, el contrato OpenAPI y el código viven juntos.
- **Visibilidad:** El revisor de la prueba técnica puede navegar todo el sistema sin saltar entre repos.
- **Docker Compose simplificado:** Las rutas relativas entre módulos son directas.
- **Atomicidad:** Un commit puede cambiar backend + frontend + infra + docs de forma atómica.
- **Frontend moderno:** React 18+ con Vite proporciona un DX ágil, hot-reload y build optimizado para producción.
- **Quality Gate explícito:** La cobertura mínima de 85% en el backend (JaCoCo) asegura calidad de código desde el inicio.

### Negativas
- **Acoplamiento de lifecycle:** Si el frontend necesita deploy independiente, requiere configuración adicional en CI/CD.
- **Escalabilidad limitada:** Si el equipo crece a >5 desarrolladores, considerar migración a multirepo.

### Riesgos
- **Riesgo:** Que el monorepo crezca descontroladamente.  
  **Mitigación:** La estructura por módulos (backend/, frontend/, infra/, docs/) impone disciplina.
- **Riesgo:** CI/CD lento al reconstruir todo.  
  **Mitigación:** Configurar triggers selectivos por ruta de archivo (solo reconstruir backend si cambió backend/).

---

## 5. Criterios de Revisión

Esta decisión debe revisarse si:
- El equipo crece a más de 5 desarrolladores activos.
- Se agregan más de 3 servicios/módulos adicionales.
- Se requiere deploy independiente con frecuencias muy diferentes.
- Se integra con otros sistemas externos que requieran contratos versionados de forma independiente.

---

## 6. Referencias

- [System Landscape](../system-landscape.md)
- [Workspace Mapping](../workspace-mapping.md)
- [Context Map](../context-map.md)
