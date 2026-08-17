# OBDX · Puntos de ranking (`rank_score`)

## Qué es

Cada prueba (evento) OBDX obtiene una **puntuación de ranking numérica de 0 a 1000**, guardada en
`k9x.events.rank_score`. Es el **dato primario**, y lo único que se persiste. La letra `rank` (`E`, `D`, `C`,
`B`, `A`, `S`) **no se guarda**: se **computa en lectura** a partir de `rank_score`
(ver [`EventSnapshot.rank()`](../k9x-backend-domain/src/main/java/com/k9x/domain/events/aggregates/EventSnapshot.java)).

La puntuación refleja lo "fuerte" que es la prueba y depende de dos factores:

1. **La configuración** (`configuration_id`): cada configuración ocupa una **franja** `[min, max]` dentro del
   0–1000. Cuanto más alto el grado, más alta la franja.
2. **La categoría** (`obdx.event_info.category`): subdivide esa franja. Un concurso de club y una final del
   mundial del mismo grado no optan a la misma puntuación.
3. Dentro de la sub-banda de la categoría, **el número de competidores** (un *tier* 1–3) posiciona la prueba.

> El viejo flag `international` (calculado a partir del país de los perros) **ya no existe**: ni en el código,
> ni en base de datos, ni en la letra — el sufijo `+` ha desaparecido. Lo que distinguía de verdad a dos
> pruebas del mismo grado era su nivel competitivo, y eso es justo lo que declara la categoría.

## Franjas por configuración

Las franjas son una **regla fija** (no cambian entre versiones de una configuración), así que viven en
**código**, no en los `configuration.json`. Se definen en el enum de dominio
[`ObdxConfigurationsRankThresholds`](../k9x-backend-domain/src/main/java/com/k9x/domain/disciplines/obdx/ObdxConfigurationsRankThresholds.java),
que resuelve la franja a partir del `configuration_id` **ignorando el sufijo de versión** `.V0` (regex
`\.V\d+$`), de modo que `OBDX_FCI_GRADE_3.V0`, `.V1`, … comparten franja.

| Configuración | Franja |
|---|---|
| `OBDX_ENCI_PREDEBUTTANTI` | 50 – 100 |
| `OBDX_ENCI_DEBUTTANTI`, `OBDX_RSCE_DEBUTANTE`, `CPC_COBS` | 100 – 200 |
| `OBDX_FCI_GRADE_1`, `OBDX_RSCE_GRADO_1` | 201 – 400 |
| `OBDX_FCI_GRADE_2` | 401 – 600 |
| `OBDX_FCI_GRADE_3` | 601 – **1000** |

## Categorías y sub-bandas

`ObdxEventCategory` tiene cinco valores: `CLUB`, `OPEN`, `WC_Q`, `WC_SEMI`, `WC_FINAL`. **Solo GRADE_3 admite
las tres de mundial**; el resto de configuraciones se limita a `CLUB` y `OPEN`. La regla vive en
`ObdxConfigurationsRankThresholds.allows(category)` y es la fuente única: la usan tanto la validación de
escritura como el catálogo de categorías.

En los grados sin mundial, **`CLUB` se queda con los 3/4 bajos de la franja y `OPEN` con el cuarto alto**
(corte en `min + round(0.75 · range)`):

```
PREDEBUTTANTI  [50, 100]     CLUB [ 50,  88]   OPEN [ 89, 100]
DEBUTTANTI /
RSCE_DEBUTANTE /
CPC_COBS       [100, 200]    CLUB [100, 175]   OPEN [176, 200]
GRADE_1 /
RSCE_GRADO_1   [201, 400]    CLUB [201, 350]   OPEN [351, 400]
GRADE_2        [401, 600]    CLUB [401, 550]   OPEN [551, 600]
GRADE_3        [601, 1000]   CLUB [601, 700]   OPEN [701, 750]
                             WC_Q [775, 850]   WC_SEMI = 900   WC_FINAL = 1000
```

`WC_Q` sí es una banda: sus tres tiers valen **800 / 825 / 850** (el suelo 775 no es alcanzable, como en el
resto de sub-bandas). `WC_SEMI` y `WC_FINAL` son **puntos fijos**: una final vale 1000 tenga los competidores
que tenga. Los huecos (751–774, 851–899, 901–999) son intencionados: nada salvo una ronda de mundial puntúa
ahí.

## Fórmula (`ObdxConfigurationsRankThresholds.eventScore`)

Con `[subMin, subMax]` la sub-banda de la categoría y `range = subMax - subMin`:

```
rank_score = subMin + round( tier/3 · range )
```

Los umbrales de tier por nº de competidores: `<10 → 1`, `[10,25) → 2`, `≥25 → 3`.

El tier **nunca cae en el suelo de la sub-banda**, y eso es deliberado: ese suelo es también el punto contra
el que se mide la puntuación de cada competidor (ver [`obdx-competitor-event-score.md`](obdx-competitor-event-score.md)),
así que una prueba que aterrizara exactamente en él dejaría a todos sus competidores aprobados empatados. Con
`WC_SEMI` y `WC_FINAL` `range` es 0, de modo que el tier no las mueve.

### Ejemplo

`CPC_COBS` (franja `[100, 200]`), categoría `CLUB` (sub-banda `[100, 175]`, `range = 75`), **3 competidores**
(→ tier 1):

```
100 + round(1/3 · 75) = 100 + 25 = 125   → etiqueta "E"
```

## La `S` ya no es manual

El tramo **901–1000** lo alcanza la fórmula automática por una única vía: una `WC_FINAL` de GRADE_3, que vale
1000 fijo. Ya no hay tope automático (`MAX_AUTOMATIC_SCORE` desapareció) ni seed manual para la `S`.

## La letra se deriva del score (rangos globales)

`rank` no se calcula por su cuenta: la **letra** se lee de la posición del `rank_score` en la escala global
0–1000 (`ObdxRank.fromScore`), y `ObdxRank.labelFromScore(score)` devuelve la letra a secas:

| rank_score | letra |
|---|---|
| ≤ 200 | E |
| 201 – 400 | D |
| 401 – 600 | C |
| 601 – 800 | B |
| 801 – 900 | A |
| 901 – 1000 | S |

Como cada franja de configuración cae dentro de uno de estos rangos, **la letra refleja el grado** de la
prueba (COBS → E, GRADE_1 → D, GRADE_2 → C, GRADE_3 → B/A/S) y la categoría la posiciona dentro. En GRADE_3 la
categoría puede empujar de **B** (club, open, clasificatoria) a **A** (semifinal) o **S** (final).

## Qué se persiste y qué se computa

Al **actualizar la prueba** (`UpdateObdxEventServiceCase.updateEvent`) se computa y persiste en `k9x.events`
una sola cosa: `rank_score` (INTEGER). Si la configuración no tiene franja, `rank_score` es `null` (y `rank()`
devuelve `null`). La **letra `rank` no se guarda**: se deriva en cada lectura con `EventSnapshot.rank()`, así
que no hay valor duplicado que mantener sincronizado.

La `category` es **obligatoria** (`obdx.event_info.category NOT NULL`): sin ella no se puede situar la prueba,
así que el caso de uso rechaza la actualización (`EventCategoryRequiredException`). Y si la categoría no es una
de las que admite la configuración, también (`EventCategoryNotAllowedException`).

El **snapshot del cron guarda solo la parte pesada `obdx`** (totales, posiciones, scores por ejercicio); los
metadatos del evento y la letra se **reconstruyen en lectura** a partir del detalle del evento (los joins que
ya hace `GetEventClassificationServiceCase`). Objetivo: cachear solo el cálculo caro de puntuaciones.

## Dónde está cada pieza

| Pieza | Fichero |
|---|---|
| Letra global + derivación (`fromScore`/`labelFromScore`) | `k9x-backend-domain/.../disciplines/obdx/ObdxRank.java` |
| Franjas, sub-bandas por categoría y fórmula del score | `k9x-backend-domain/.../disciplines/obdx/ObdxConfigurationsRankThresholds.java` |
| Categorías | `k9x-backend-domain/.../disciplines/obdx/ObdxEventCategory.java` |
| Letra derivada del evento | `k9x-backend-domain/.../events/aggregates/EventSnapshot.java` (`rank()`) |
| Cálculo y validación al actualizar la prueba | `k9x-backend-application/.../events/obdx/use_case/UpdateObdxEventServiceCase.java` |
| Ensamblado en lectura (snapshot obdx + metadatos) | `k9x-backend-application/.../events/use_case/GetEventClassificationServiceCase.java` |
| Persistencia (escritura/lectura) | `SaveCompetitionJooqAdapter.java` / `CompetitionHydrator.java` |
| Columnas | `V1__create_mvp_db.sql` (`events.rank_score`, `obdx.event_info.category`) |

## rank_score por competidor

Además del `rank_score` del evento, cada competidor tiene su propio `rank_score`, persistido en
`obdx.snap_event_competitors_results.rank_score` (`NUMERIC(6,2)`). **Premia el mérito** (subir por los
calificativos), no solo ir a un evento con mucho campo. Resumen:

- **Descalificado** (tarjeta roja, dos amarillas o no compite) → **NULL**: el evento no entra en el historial
  del perro.
- No llega a la 1ª qualificación → **suelo del grado menos uno** (`gradeFloor − 1`, p.ej. 600 en GRADE_3),
  independientemente de la categoría.
- Llegar a la 1ª qualificación desbloquea un **10 % fijo** de `(eventScore − gradeFloor)`.
- El **90 % restante** tiene la **rodilla en el calificativo más alto** de la config (p.ej. EXC): de la 1ª al
  top qualificativo se gana el **85 %** de esa ventana; del top al máximo, el 15 % (pulido). Un 100 % cae justo
  en `eventScore`.
- Sin puntuación (sin scores) → **NULL**.

La fórmula completa, con ejemplos y tablas, está en **[`obdx-competitor-event-score.md`](obdx-competitor-event-score.md)**
(dominio: `ObdxCompetitorEventScore`).

Se calcula en `GetObdxClassificationServiceCase` (junto a la posición) y se **persiste en el cron diario de
snapshot** (`GenerateEventSnapshotsServiceCase`): posición + `rank_score` del competidor + fila de snapshot se
escriben **atómicamente** en una sola transacción (`SaveObdxSnapshotJooqAdapter`, `dsl.transaction`). Un fallo
deja la prueba sin snapshot y se reintenta al día siguiente (escrituras idempotentes).

## Cómo cambiar las franjas o las sub-bandas

Editar los valores en `ObdxConfigurationsRankThresholds`: las franjas están en los constructores del enum, el
corte CLUB/OPEN en `CLUB_SHARE`, y el layout de GRADE_3 (incluidos los tres puntos de mundial) en sus
constantes. No hay que tocar JSON ni base de datos, más allá de recalcular las pruebas afectadas volviéndolas
a guardar.
