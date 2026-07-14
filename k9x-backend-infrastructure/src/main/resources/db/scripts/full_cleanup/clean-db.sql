-- FULL CLEANUP: borra TODO el estado de la base de datos k9x.
-- Dropea los esquemas de datos (k9x + obdx) y las tablas de historial de Flyway.
-- Al arrancar la app, Flyway re-ejecuta las migraciones desde cero (V1 en adelante)
-- y regenera los checksums, resolviendo el "checksum mismatch".
-- ADVERTENCIA: destruye tablas y datos de forma irreversible.

-- 1. Esquemas de datos (V1 los recrea con CREATE SCHEMA k9x / obdx)
DROP SCHEMA IF EXISTS obdx CASCADE;
DROP SCHEMA IF EXISTS k9x CASCADE;

-- 2. Historial de Flyway (viven en el esquema por defecto de la conexión, p.ej. public).
--    Sin esto, Flyway conserva el checksum viejo de V1 y vuelve a fallar la validación.
DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS flyway_data_history;
