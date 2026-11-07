# GymFlow — Backend

API REST de GymFlow construida con Java 21, Spring Boot 3.5, PostgreSQL, Spring Data JPA/JDBC, Flyway, Spring Security y Swagger/OpenAPI.

## Requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop para PostgreSQL local
- Docker Engine solo para pruebas de integración con Testcontainers

## Ejecutar localmente

```bash
# Iniciar PostgreSQL
docker compose up -d postgres

# Iniciar API
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

Flyway aplica las migraciones al iniciar el backend.

## Configuración

Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `GYMFLOW_DEV_USER`, `GYMFLOW_DEV_PASSWORD` y `PORT`. Los valores predeterminados de `application.yml` son solo para desarrollo local. No publiques credenciales de desarrollo.

## Pruebas

### Unitarias — no requieren Docker

```bash
mvn clean test
```

### Integración PostgreSQL — requieren Docker

```bash
mvn -Pintegration-tests clean verify
```

El perfil de integración ejecuta `GymFlowIntegrationTest` con Testcontainers y PostgreSQL temporal. No utiliza el volumen `gymflow_pgdata` del entorno local. Las dependencias Testcontainers tienen alcance `test` y no forman parte del runtime de la API.

## Recursos principales

- `/api/v1/gyms`
- `/api/v1/gyms/{gymId}/members`
- `/api/v1/gyms/{gymId}/membership-plans`
- `/api/v1/gyms/{gymId}/memberships`
- `/api/v1/gyms/{gymId}/categories`
- `/api/v1/gyms/{gymId}/suppliers`
- `/api/v1/gyms/{gymId}/products`
- `/api/v1/gyms/{gymId}/products/{productId}/history`
- `/api/v1/gyms/{gymId}/inventory/balances`
- `/api/v1/gyms/{gymId}/inventory/movements`
- `/api/v1/gyms/{gymId}/sales`

Consulta Swagger para métodos, payloads y respuestas concretas.

## Alcance y límites

- Demo de una sola instalación/gimnasio.
- HTTP Basic temporal para desarrollo; no es autenticación/autoridad productiva.
- Sin pasarela de pago ni correos reales.
- No utilizar datos reales ni exponer credenciales locales.
- El README raíz y `docs/TECHNICAL-DOCUMENTATION.md` contienen la guía completa y las limitaciones conocidas.
