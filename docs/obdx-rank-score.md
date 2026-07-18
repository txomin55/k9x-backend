# OBDX · Puntos de ranking (`rank_score`)

## Qué es

Cada prueba (evento) OBDX obtiene una **puntuación de ranking numérica de 0 a 1000**, guardada en
`k9x.events.rank_score`. Es el **dato primario**. Junto a él se persiste solo un booleano `k9x.events.international`.
La letra `rank` (`E`, `E+`, … `A`, `A+`, `S`, `S+`) **no se guarda**: se **computa en lectura** a partir de
`rank_score` + `international` (ver [`EventSnapshot.rank()`](../k9x-backend-domain/src/main/java/com/k9x/domain/events/aggregates/EventSnapshot.java)).

La puntuación refleja lo "fuerte" que es la prueba y depende de tres factores:

1. **La configuración** (`configuration_id`): cada configuración ocupa una **franja** `[min, max]` dentro del
   0–1000. Cuanto más alta la categoría, más alta la franja.
2. **El número de competidores**: coloca la prueba dentro de la franja (más competidores → más arriba).
3. **Si es internacional**: hay al menos un competidor cuyo perro es de un país distinto al de la
   competición.

## Franjas por configuración

Las franjas son una **regla fija** (no cambian entre versiones de una configuración), así que viven en
**código**, no en los `configuration.json`. Se definen en el enum de dominio
[`ObdxConfigurationsRankThresholds`](../k9x-backend-domain/src/main/java/com/k9x/domain/disciplines/obdx/ObdxConfigurationsRankThresholds.java),
que resuelve la franja a partir del `configuration_id` **ignorando el sufijo de versión** `.V0` (regex
`\.V\d+$`), de modo que `OBDX_FCI_GRADE_3.V0`, `.V1`, … comparten franja.

| Configuración | Franja |
|---|---|
| `OBDX_RSCE_DEBUTANTE`, `CPC_COBS` | 100 – 200 |
| `OBDX_FCI_GRADE_1`, `OBDX_RSCE_GRADE_1` | 201 – 400 |
| `OBDX_FCI_GRADE_2` | 401 – 600 |
| `OBDX_FCI_GRADE_3` | 601 – 900 |

## Fórmula (`ObdxConfigurationsRankThresholds.eventScore`)

Con `range = max - min` de la franja de la configuración:

- El **`+` internacional** vale un **10 % del range** (fijo).
- El **90 % restante** se reparte por un **tier (capa) 1–5 según el nº de competidores** (esto **no** es la
  letra `rank`, es solo una capa para posicionar dentro de la franja): el tier aporta `tier / 5 · 90% · range`.

```
rank_score = min
           + redondear( tier/5 · 0.9 · range )
           + ( internacional ? redondear(0.1 · range) : 0 )
```

Los umbrales de tier por nº de competidores:
`<5 → 1`, `[5,10) → 2`, `[10,20) → 3`, `[20,35) → 4`, `≥35 → 5`.

### Ejemplo

`CPC_COBS` (franja `[100, 200]`, `range = 100`), **3 competidores** (→ tier 1) con **1 internacional**:

```
100 + round(1/5 · 0.9 · 100)  + round(0.1 · 100)
100 +        18               +        10          = 128   → etiqueta "E+"
```

## El tope 900 y `S`

La fórmula automática **nunca supera 900** (`ObdxRank.MAX_AUTOMATIC_SCORE`). El tramo **901–1000 queda
reservado** para el rango **`S`**, que **solo se asigna manualmente** (seed, p. ej. una final de campeonato
del mundo) y es **siempre internacional** (por eso se etiqueta `S+`). Por eso todas las franjas de
configuración terminan en ≤ 900 y **GRADE_3 nunca llega a S** (su franja `[601, 900]` topa en A).

## La letra se deriva del score (rangos globales)

`rank` no se calcula por su cuenta: la **letra** se lee de la posición del `rank_score` en la escala global
0–1000 (`ObdxRank.fromScore`), y el **`+`** marca que el evento es internacional
(`ObdxRank.labelFromScore(score, internacional)`):

| rank_score | letra |
|---|---|
| ≤ 200 | E |
| 201 – 400 | D |
| 401 – 600 | C |
| 601 – 800 | B |
| 801 – 900 | A |
| 901 – 1000 | S (manual, siempre internacional → `S+`) |

Como cada franja de configuración cae dentro de uno de estos rangos, **la letra refleja la categoría** de la
prueba (COBS → E, GRADE_1 → D, GRADE_2 → C, GRADE_3 → B/A) y el score la posiciona dentro. En GRADE_3
(`[601, 900]`) el nº de competidores puede empujar de **B** a **A**.

## Qué se persiste y qué se computa

Al **actualizar la prueba** (`UpdateObdxEventServiceCase.updateEvent`) se computan y persisten en `k9x.events`
solo dos cosas: `rank_score` (INTEGER) e `international` (BOOLEAN). Si la configuración no tiene franja,
`rank_score` es `null` (y `rank()` devuelve `null`). La **letra `rank` no se guarda**: se deriva en cada
lectura con `EventSnapshot.rank()`, así que no hay valor duplicado que mantener sincronizado.

El **snapshot del cron guarda solo la parte pesada `obdx`** (totales, posiciones, scores por ejercicio); los
metadatos del evento y la letra se **reconstruyen en lectura** a partir del detalle del evento (los joins que
ya hace `GetEventClassificationServiceCase`). Objetivo: cachear solo el cálculo caro de puntuaciones.

## Dónde está cada pieza

| Pieza | Fichero |
|---|---|
| Letra global + tope + derivación (`fromScore`/`labelFromScore`/`rangeFloor`) | `k9x-backend-domain/.../disciplines/obdx/ObdxRank.java` |
| Franjas por config + fórmula del score (`eventScore`, tier por nº competidores) | `k9x-backend-domain/.../disciplines/obdx/ObdxConfigurationsRankThresholds.java` |
| Letra derivada del evento | `k9x-backend-domain/.../events/aggregates/EventSnapshot.java` (`rank()`) |
| Cálculo al actualizar la prueba | `k9x-backend-application/.../events/obdx/use_case/UpdateObdxEventServiceCase.java` |
| Ensamblado en lectura (snapshot obdx + metadatos) | `k9x-backend-application/.../events/use_case/GetEventClassificationServiceCase.java` |
| Persistencia (escritura/lectura) | `SaveCompetitionJooqAdapter.java` / `CompetitionHydrator.java` |
| Columnas | `V1__create_mvp_db.sql` (`events.rank_score`, `events.international`) |

## rank_score por competidor

Además del `rank_score` del evento, cada competidor tiene su propio `rank_score` en
`obdx.event_competitors.rank_score` (`NUMERIC(6,2)`). **Premia el mérito** (subir por los calificativos), no
solo ir a un evento con mucho campo. Resumen:

- No llega a la 1ª qualificación → **suelo del rango anterior** (`configBandMin − 1`).
- Llegar a la 1ª qualificación desbloquea un **10 % fijo** de `(eventScore − configBandMin)`.
- El **90 % restante** tiene la **rodilla en el calificativo más alto** de la config (p.ej. EXC): de la 1ª al
  top qualificativo se gana el **85 %** de esa ventana; del top al máximo, el 15 % (pulido). Un 100 % cae justo
  en `eventScore`.
- Sin puntuación (no compiten / sin scores) → **NULL**.

La fórmula completa, con ejemplo y tabla, está en **[`obdx-competitor-event-score.md`](obdx-competitor-event-score.md)**
(dominio: `ObdxCompetitorEventScore`).

Se calcula en `GetObdxClassificationServiceCase` (junto a la posición) y se **persiste en el cron diario de
snapshot** (`GenerateEventSnapshotsServiceCase`): posición + `rank_score` del competidor + fila de snapshot se
escriben **atómicamente** en una sola transacción (`SaveObdxSnapshotJooqAdapter`, `dsl.transaction`). Un fallo
deja la prueba sin snapshot y se reintenta al día siguiente (escrituras idempotentes).

## Cómo cambiar las franjas

Editar los valores en `ObdxConfigurationsRankThresholds`. No hay que tocar JSON ni base de datos (más allá de
recalcular las pruebas afectadas volviéndolas a guardar). Mantener siempre `max ≤ 900` (901–1000 es zona `S`
manual).
