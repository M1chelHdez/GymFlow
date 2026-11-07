# Preparar publicación en GitHub

1. Revisa `git status` y asegúrate de que no hay `.env`, secretos, `target/`, `node_modules/` ni `dist/`.
2. Ejecuta las pruebas unitarias del backend y build de Angular local.
3. Para el frontend público, configura `environment.public-demo.ts` con la URL pública del backend (esa URL no es secreta) y ejecuta `npm run build:public-demo`.
4. Usa el perfil Spring `public-demo` solo en la instalación compartida de portfolio. Para local conserva la configuración de desarrollo.
5. Revisa diff y archivos incluidos en el commit; si alguna credencial real fue subida antes, revócala/rotala antes de limpiar el historial.
6. Commit sugerido: `chore: prepare GymFlow public portfolio demo`.
7. Tag sugerido: `v1.0.0-demo`.

La demo pública no incluye login y permite escritura compartida para mantener los módulos interactivos. No la uses como sistema productivo.
