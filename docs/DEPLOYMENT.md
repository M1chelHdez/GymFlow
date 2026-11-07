# GymFlow — despliegue de demo pública

Esta carpeta describe la configuración de la **demo pública de portafolio**. La configuración local que usa HTTP Basic y `localhost` se mantiene para desarrollo.

## Arquitectura sugerida

- Angular estático: Cloudflare Pages.
- Spring Boot: Render Web Service (Docker).
- PostgreSQL: Neon u otro proveedor PostgreSQL gestionado.

Los planes gratuitos pueden dormir, limitar conexiones o no ofrecer respaldo garantizado. Verifica cuotas y políticas actuales antes de desplegar.

## Modo público de demostración

La demo se inicia con `SPRING_PROFILES_ACTIVE=public-demo`. Este perfil:

- No utiliza el interceptor Angular que inserta HTTP Basic fijo.
- Permite peticiones API sin login para que un visitante pueda utilizar los módulos desde el portafolio.
- Restringe CORS al origen configurado en `GYMFLOW_DEMO_ALLOWED_ORIGIN`.
- Deshabilita Swagger/OpenAPI público.
- Requiere las credenciales de PostgreSQL por variables de entorno; no define defaults de DB en este perfil.

**Advertencia:** para que todos los módulos sigan siendo interactivos, la demo pública es una instalación compartida y sus operaciones de escritura están abiertas. Visitantes podrían crear, editar o eliminar registros y ver los datos de demostración compartidos. Utiliza exclusivamente información ficticia; no cargues datos personales, datos financieros reales ni credenciales. Esto es un entorno de portfolio, no un producto productivo. La API no tiene autenticación de usuarios, roles ni aislamiento por visitante.

## Variables del backend

Configura en el proveedor de hosting (no en Git):

- `SPRING_PROFILES_ACTIVE=public-demo`
- `DB_URL`: JDBC PostgreSQL suministrado por el proveedor, incluyendo SSL cuando se requiera.
- `DB_USERNAME`: usuario de base de datos.
- `DB_PASSWORD`: secreto de base de datos.
- `GYMFLOW_DEMO_ALLOWED_ORIGIN`: origen exacto de Cloudflare Pages, por ejemplo `https://gymflow-demo.pages.dev` (sin ruta).
- `PORT`: puerto asignado por el proveedor, si aplica.

Flyway ejecuta las migraciones al iniciar. Asegúrate de que la base de datos esté vacía o contenga un esquema compatible.

## Variables del frontend

Antes del build de Cloudflare Pages, edita `gymflow-frontend/src/environments/environment.public-demo.ts` y sustituye `https://YOUR-GYMFLOW-BACKEND.example.com` por el origen real del backend. La URL de la API es pública y no debe contener contraseñas ni tokens.

- Root directory: `gymflow-frontend`
- Build command: `npm install && npm run build:public-demo`
- Output directory: `dist/gymflow-frontend/browser`

`build:public-demo` produce el frontend optimizado y elimina los mapas de source code. El build local normal permanece disponible.

## Lista de verificación de publicación

1. Crea repositorio público desde la carpeta `GymFlow/`; verifica que `.env`, `target/`, `node_modules/` y secretos no estén incluidos.
2. Provisiona PostgreSQL; registra DB_URL/DB_USERNAME/DB_PASSWORD como variables protegidas en el hosting backend.
3. Configura `SPRING_PROFILES_ACTIVE=public-demo` y el origen Pages exacto en CORS.
4. Sustituye el host placeholder en `environment.public-demo.ts`; configura Cloudflare Pages y build.
5. Prueba health endpoint, apertura de la app y CRUD de socios, planes, membresías, productos, movimientos, ventas, reportes y exportación.
6. Confirma que la base conserva cambios tras un redeploy/reinicio del servicio.
7. Nunca cargues información real. Revisa uso y almacenamiento del proveedor.

No se afirma que el despliegue haya sido realizado o validado en los proveedores; requiere los endpoints, secretos y cuentas del propietario.
