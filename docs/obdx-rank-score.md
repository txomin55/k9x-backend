# OBDX · Puntos de ranking (`rank_score`)

## Qué es

Cada prueba (evento) OBDX obtiene una **puntuación de ranking numérica de 0 a 1000**, guardada en
`k9x.events.rank_score`. Es el **dato primario**: la letra `rank` (`E`, `E+`, … `A`, `A+`, `A++`) ahora es
una **etiqueta derivada** de ese número.

La puntuación refleja lo "fuerte" que es la prueba y depende de tres factores:

1. **La configuración** (`configuration_id`): cada configuración ocupa una **franja** `[min, max]` dentro del
   0–1000. Cuanto más alta la categoría, más alta la franja.
2. **El número de competidores**: coloca la prueba dentro de la franja (más competidores → más arriba).
3. **Si es internacional**: hay al menos un competidor cuyo perro es de un país distinto al de la
   competición.

## Franjas por configuración

Las franjas son una **regla fija** (no cambian entre versiones de una configuración), así que viven en
**código**, no en los `configuration.json`. Se definen en el enum de dominio
[`ObdxRankBand`](../k9x-backend-domain/src/main/java/com/k9x/domain/disciplines/obdx/ObdxRankBand.java),
que resuelve la franja a partir del `configuration_id` **ignorando el sufijo de versión** `.V0` (regex
`\.V\d+$`), de modo que `OBDX_FCI_GRADE_3.V0`, `.V1`, … comparten franja.

| Configuración | Franja |
|---|---|
| `OBDX_RSCE_DEBUTANTE`, `CPC_COBS` | 100 – 200 |
| `OBDX_FCI_GRADE_1`, `OBDX_RSCE_GRADE_1` | 201 – 400 |
| `OBDX_FCI_GRADE_2` | 401 – 600 |
| `OBDX_FCI_GRADE_3` | 601 – 950 |

## Fórmula

Con `range = max - min` de la franja de la configuración:

- El **`+` internacional** vale un **10 % del range** (fijo).
- El **90 % restante** se reparte entre las 5 letras según el nº de competidores
  (`E=1, D=2, C=3, B=4, A=5`): la letra aporta `tier / 5 · 90% · range`.

```
rank_score = min
           + redondear( tier/5 · 0.9 · range )
           + ( internacional ? redondear(0.1 · range) : 0 )
```

Los umbrales de letra por nº de competidores son los de siempre:
`E < 5`, `D [5,10)`, `C [10,20)`, `B [20,35)`, `A ≥ 35`.

### Ejemplo

`CPC_COBS` (franja `[100, 200]`, `range = 100`), **3 competidores** (→ `E`) con **1 internacional**:

```
100 + round(1/5 · 0.9 · 100)  + round(0.1 · 100)
100 +        18               +        10          = 128   → etiqueta "E+"
```

## El tope 950 y `A++`

La fórmula automática **nunca supera 950** (`ObdxRank.MAX_AUTOMATIC_SCORE`). El tramo **951–1000 queda
reservado** para el rango **`A++`** (`EXCEPTIONAL`), que **solo se asigna manualmente** (seed, p. ej. una
final de campeonato del mundo). Por eso todas las franjas de configuración terminan en ≤ 950.

Al derivar la letra desde el score, un `rank_score > 950` se etiqueta siempre como `A++`.

## La letra se deriva del score (rangos globales)

`rank` deja de calcularse por su cuenta: la **letra** se lee de la posición del `rank_score` en la escala
global 0–1000 (`ObdxRank.fromScore`), y el **`+`** marca que el evento es internacional
(`ObdxRank.labelFromScore(score, internacional)`):

| rank_score | letra |
|---|---|
| ≤ 200 | E |
| 201 – 400 | D |
| 401 – 600 | C |
| 601 – 800 | B |
| 801 – 1000 | A |

Como cada franja de configuración cae dentro de uno de estos rangos, **la letra refleja la categoría** de la
prueba (COBS → E, GRADE_1 → D, GRADE_2 → C, GRADE_3 → B/A) y el score la posiciona dentro. En GRADE_3
(`[601, 950]`) el nº de competidores puede empujar de **B** a **A**.

## Cuándo se calcula

Igual que antes con la letra: **una sola vez al actualizar la prueba**
(`UpdateObdxEventServiceCase.updateEvent`). Se computa `rank_score`, se deriva `rank`, y ambos se persisten
en `k9x.events` (columnas `rank_score` y `rank`). Si la configuración no tiene franja definida, `rank_score`
es `null` y `rank` cae al comportamiento antiguo (letra + `+`).

## Dónde está cada pieza

| Pieza | Fichero |
|---|---|
| Fórmula + derivación de letra + tope | `k9x-backend-domain/.../disciplines/obdx/ObdxRank.java` |
| Franjas por configuración | `k9x-backend-domain/.../disciplines/obdx/ObdxRankBand.java` |
| Cálculo al actualizar la prueba | `k9x-backend-application/.../events/obdx/use_case/UpdateObdxEventServiceCase.java` |
| Persistencia (escritura) | `k9x-backend-infrastructure/.../competitions/SaveCompetitionJooqAdapter.java` |
| Persistencia (lectura/hidratación) | `k9x-backend-infrastructure/.../competitions/CompetitionHydrator.java` |
| Columna | `k9x-backend-infrastructure/src/main/resources/db/schema/V1__create_mvp_db.sql` (`events.rank_score`) |

## rank_score por competidor

Además del `rank_score` del evento, cada competidor tiene su propio `rank_score` en
`obdx.event_competitors.rank_score` (`NUMERIC(6,2)`), que proyecta su rendimiento en la prueba sobre la banda
del evento:

```
floor = límite inferior del rango de letra del rank_score del evento   (p.ej. evento 550 → C → 401)
span  = rank_score_evento − floor
rank_score_competidor = floor + span × (total_competidor / max_total)
```

- `total_competidor` = suma de las notas ponderadas por coeficiente (misma agregación AVG/MID_AVG que la
  clasificación); `max_total` = `maxAllowedScore × Σcoef` de la prueba.
- Un 100 % de rendimiento obtiene el `rank_score` del evento; un 0 %, el `floor`.
- Competidores **sin puntuación** (no compiten / sin scores) → `rank_score` **NULL**.
- Ejemplo: evento 550 (C, 401–600), competidor 160/320 → `401 + 149 × 0.5 = 475.50`.

Se calcula en `GetObdxClassificationServiceCase` (junto a la posición) y se **persiste en el cron diario de
snapshot** (`GenerateEventSnapshotsServiceCase`): posición + `rank_score` del competidor + fila de snapshot se
escriben **atómicamente** en una sola transacción (`SaveObdxSnapshotJooqAdapter`, `dsl.transaction`). Un fallo
deja la prueba sin snapshot y se reintenta al día siguiente (escrituras idempotentes).

## Cómo cambiar las franjas

Editar los valores en `ObdxRankBand`. No hay que tocar JSON ni base de datos (más allá de recalcular las
pruebas afectadas volviéndolas a guardar). Mantener siempre `max ≤ 950`.
