# eSoluzion Pricing API

Sistema de consulta de tarifas/precios de productos para cadenas de comercio electrónico.
Prueba técnica Spring Boot con Arquitectura Hexagonal.

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21+ |
| Framework | Spring Boot 3.3.0 |
| Build | Gradle (Kotlin DSL) 8.7 |
| Base de datos | H2 (en memoria) |
| Migraciones | Flyway |
| API | OpenAPI 3.0.3 (API First) |
| Generación código | OpenAPI Generator (interfaceOnly) |
| Mapeo | MapStruct |
| Tests | JUnit 5 + Mockito + AssertJ |
| Cobertura | JaCoCo (≥85%) |

## Estructura del Proyecto

```
eSoluzion-prueba/
├── backend/                    # API REST (Spring Boot + Hexagonal)
│   ├── src/main/java/.../
│   │   ├── domain/            # Entidades, Value Objects, Excepciones
│   │   ├── application/       # Puertos, Casos de Uso, Modelos
│   │   └── infrastructure/    # Adaptadores (JPA, Web, Config)
│   ├── src/main/resources/
│   │   ├── application.yml    # Configuración Spring Boot
│   │   ├── openapi.yaml       # Contrato OpenAPI (runtime)
│   │   └── db/migration/      # Migraciones Flyway
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── frontend/                   # Frontend React (incremento 002)
├── infra/                      # Docker Compose + Dockerfiles
│   ├── docker-compose.yml
│   ├── Dockerfile.backend
│   └── Dockerfile.frontend
└── docs/                       # Documentación
    ├── api/pricing-api.yaml    # OpenAPI canónico (fuente de verdad)
    ├── architecture/           # Documentos de arquitectura enterprise
    └── specs/                  # Especificaciones SDD
```

## Endpoint

```
GET /api/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
```

### Parámetros

| Parámetro | Tipo | Obligatorio | Descripción |
|-----------|------|-------------|-------------|
| `applicationDate` | `string` (ISO-8601) | Sí | Fecha y hora de consulta |
| `productId` | `int64` | Sí | Identificador del producto |
| `brandId` | `int64` | Sí | Identificador de la cadena (1=ZARA) |

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

## Casos de Prueba

| Test | Fecha | Producto | Cadena | Tarifa | Precio |
|------|-------|----------|--------|--------|--------|
| T1 | 2020-06-14 10:00 | 35455 | 1 (ZARA) | 1 | 35.50€ |
| T2 | 2020-06-14 16:00 | 35455 | 1 (ZARA) | 2 | 25.45€ |
| T3 | 2020-06-14 21:00 | 35455 | 1 (ZARA) | 1 | 35.50€ |
| T4 | 2020-06-15 10:00 | 35455 | 1 (ZARA) | 3 | 30.50€ |
| T5 | 2020-06-16 21:00 | 35455 | 1 (ZARA) | 4 | 38.95€ |

## Cómo ejecutar

### Local (sin Docker)

```bash
cd backend
./gradlew bootRun
```

### Tests

```bash
cd backend
./gradlew test
```

### Docker Compose

```bash
cd infra
docker compose up --build
```

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### H2 Console

```
http://localhost:8080/h2-console
```

## Arquitectura

El proyecto sigue **Arquitectura Hexagonal** (Puertos y Adaptadores):

- **Domain**: Lógica de negocio pura, sin dependencias de frameworks
- **Application**: Casos de uso, puertos de entrada/salida
- **Infrastructure**: Adaptadores (JPA, REST, etc.)

Decisión clave: **OpenAPI First** — el contrato OpenAPI en `docs/api/pricing-api.yaml` es la fuente de verdad.
