# Estrategia de pruebas de GymFlow

## Flujo rápido sin Docker

Desde `gymflow-backend`:

```bash
mvn clean test
```

Maven Surefire ejecuta las pruebas unitarias. Excluye `*IntegrationTest.java` para que el comando predeterminado no dependa de Docker.

## Suite de integración

Requiere Docker Desktop y Docker Engine activos:

```bash
mvn -Pintegration-tests clean verify
```

Maven Failsafe ejecuta las clases `*IntegrationTest.java` durante las fases `integration-test` y `verify`. Testcontainers levanta PostgreSQL temporal.

## Separación de entornos

- PostgreSQL de desarrollo: contenedor `gymflow-postgres`, volumen `gymflow_pgdata`, puerto local 5432.
- PostgreSQL de integración: contenedor temporal administrado por Testcontainers; se elimina al terminar.
- Testcontainers no es requisito para compilar, iniciar ni ejecutar la aplicación.
- Las dependencias Testcontainers usan scope Maven `test`.

## Reportes

- Surefire: `target/surefire-reports/`
- Failsafe: `target/failsafe-reports/`

Un resultado de pruebas solo debe considerarse aprobado cuando el comando termina en `BUILD SUCCESS` y el resumen muestra cero fallos y cero errores.
