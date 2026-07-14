-- Limpia TODOS los datos de la base de datos k9x (esquemas k9x + obdx).
-- No borra el esquema ni las tablas, solo vacía filas y reinicia las secuencias.
-- CASCADE resuelve el orden de foreign keys automáticamente.

TRUNCATE TABLE
    obdx.event_scores,
    obdx.event_exercises,
    obdx.event_judges,
    obdx.event_competitors,
    k9x.events,
    k9x.stages,
    k9x.competitions,
    k9x.judges,
    k9x.dogs,
    k9x.organizers,
    k9x.users
RESTART IDENTITY CASCADE;
