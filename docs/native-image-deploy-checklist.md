# Paso a native image: checklist de despliegue

## Qué tienes que hacer tú

1. **Fly:** ejecuta `fly secrets set NEW_RELIC_LICENSE_KEY=...` antes de desplegar producción. Es la key que
   estaba en `newrelic/newrelic.yml`. Ese fichero ya no está en el repo, pero la key sigue en el historial de
   git, así que conviene rotarla.
2. **Render (staging):** si tienes `NEW_RELIC_ENABLED` o `JAVA_OPTS` en el panel, bórralas. Si tienes un
   "Docker Command" personalizado que llame a `java`, quítalo también.
3. **Despliegue:** despliega primero staging y prueba ahí dos cosas que no se pudieron probar en local:
   - el login con Google completo;
   - el envío real de una notificación push.

   Esas rutas tienen hints, pero no se han ejecutado de principio a fin.

## A tener en cuenta

- **Build en CI:** en una máquina de 16 CPU la compilación nativa tarda unos 3 minutos y llega a 9 GB de RAM.
  En un runner de GitHub (4 CPU, 16 GB) cabe, pero cada despliegue será más lento.
- **Añadidos futuros:** estos cambios necesitan una entrada en `NativeImageHintsConfiguration`:
  - un recurso nuevo leído por ruta;
  - un tipo serializado a mano con `ObjectMapper`;
  - una caché Caffeine con otra configuración.

  Si falta la entrada, funciona en local y falla solo al desplegar. Está explicado en el README.
- **Carpeta `newrelic/`:** se ha borrado la carpeta local, que solo tenía los binarios descargados del agente.
- **Crear un juez sin `id`:** devuelve 500. Pasa igual en JVM, así que es un problema previo a este cambio y no
  se ha tocado.
