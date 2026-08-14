# OBDX · `competitorEventScore` (rank_score por competidor)

Cada competidor de una prueba OBDX obtiene su propio `rank_score`, persistido en
`obdx.snap_event_competitors_results.rank_score` (`NUMERIC(6,2)`) por el cron diario de snapshot. Sustituye al
viejo `floor + span·(total/max)`, que **sobre-premiaba ir a eventos con mucho campo** (la banda del competidor
se inflaba con el `rank_score` del evento). La nueva lógica **premia el mérito**: hay que subir por los
**calificativos** (qualifications) de la configuración.

## Idea

El competidor puede ganar entre `gradeFloor − 1` (no llegó) y `eventScore` (perfecto). El reparto:

0. **Descalificado** (tarjeta roja, dos amarillas o marcado como que no compite) → **NULL**. Aunque tenga
   ejercicios puntuados, la prueba no entra en su historial: ni suma nivel ni refresca la frescura del índice.
1. **No llega a la 1ª qualificación** → cae al **techo del rango anterior**: `gradeFloor − 1`
   (p.ej. GRADE_3 → 600). "No te habías ganado estar en este grado".
2. **Llegar a la 1ª qualificación** desbloquea un **10 % fijo** de `span = eventScore − gradeFloor`.
3. El **90 % restante** (la "ventana") tiene una **rodilla en el calificativo MÁS ALTO** que defina la config
   (dinámico: EXC, u otro si lo hubiera):
   - de la **1ª qualificación al top qualificativo** se gana el **85 %** de la ventana (ahí está el grueso);
   - del **top qualificativo al máximo** se gana el **15 %** restante (solo pulido).
   - Un 100 % de nota cae exactamente en `eventScore`.

La intuición de obediencia: **llegar a EXC (≈80%) ya es la excelencia**; del 80 % al 100 % solo estás puliendo.

## El suelo es el del grado, no el de la sub-banda de la categoría

`gradeFloor` es el `min` de la **configuración** (601 en GRADE_3), **no** el de la sub-banda de la categoría
del evento. Todos los competidores de un mismo grado se miden desde el mismo punto, y suspender una final del
mundial deja el mismo 600 que suspender un concurso de club.

Es deliberado: con el suelo de la sub-banda, suspender una `WC_FINAL` daría **900** y aprobarla por los pelos
**911** — ambos por encima del 700 que saca un ganador de un CLUB perfecto. Lo que sí cambia con la categoría
es el **techo**: el `eventScore`, y por tanto el `span`.

## Fórmula

```
firstQual = min(qualifications) ; topQual = max(qualifications)
max       = maxAllowedScore · Σcoef       ; total = nota ponderada del competidor
span      = eventScore − gradeFloor
window    = 0.90 · span                    ; unlock = 0.10 · span
base      = gradeFloor + unlock            ; KNEE = 0.85

descalificado                  → null
total < firstQual              → gradeFloor − 1
firstQual ≤ total ≤ topQual    → base + (total−firstQual)/(topQual−firstQual) · (KNEE·window)
topQual  < total ≤ max         → base + KNEE·window + clamp((total−topQual)/(max−topQual)) · ((1−KNEE)·window)
```

Redondeo a 2 decimales (HALF_UP). Degenerados: **sin qualifications** → sin caída y la ventana sube lineal de
`0→max`; **una sola qualification** (`topQual == firstQual`) → ventana lineal de `firstQual→max` (sin rodilla).

## Ejemplo A · GRADE_3 `OPEN`, tier 3

Evento `eventScore = 750`, grado `[601, 1000]` → `gradeFloor = 601`, `span = 149`. Calificativos
`B = 192`, `MB = 224`, `EXC = 256` (el top). `max = 320`. → `unlock = 14.9`, `window = 134.1`,
`base = 615.9`, `KNEE·window = 113.985`.

| total | % (total/max) | calificativo | competitorEventScore |
|---|---|---|---|
| < 192 | < 60% | — (no llega a B) | **600** |
| 192 | 60% | B | **615.90** |
| 224 | 70% | MB | 672.89 |
| 256 | 80% | **EXC (rodilla)** | **729.89** |
| 288 | 90% | — | 739.94 |
| 320 | 100% | — | **750.00** |

## Ejemplo B · GRADE_3 `WC_FINAL`

Evento `eventScore = 1000` (fijo para una final) → `span = 399`, `unlock = 39.9`, `base = 640.9`,
`window = 359.1`, `KNEE·window = 305.235`.

| total | calificativo | competitorEventScore |
|---|---|---|
| < 192 | — (suspenso) | **600** |
| 192 | B | **640.90** |
| 224 | MB | 793.52 |
| 256 | **EXC (rodilla)** | 946.14 |
| 288 | — | 973.07 |
| 320 | — | **1000.00** |

Nótese el contraste entre las dos tablas: es el mismo perro con la misma nota, pero la final le vale mucho
más. Y un 100 % en la final cae en 1000, es decir, letra `S`.

## Dónde vive

| Pieza | Fichero |
|---|---|
| Fórmula | `k9x-backend-domain/.../disciplines/obdx/ObdxCompetitorEventScore.java` |
| Cálculo (total/max + calificativos + descalificación + franja) | `k9x-backend-application/.../events/obdx/use_case/GetObdxClassificationServiceCase.java` (inline en `aggregateProjection`) |
| Persistencia (cron transaccional) | `GenerateEventSnapshotsServiceCase` → `SaveObdxSnapshotJooqAdapter` |
| Columna | `obdx.snap_event_competitors_results.rank_score` (`NUMERIC(6,2)`) |
| Historial del perro | `k9x.snap_dog_rank` (solo filas con `rank_score` no nulo) |

## Cómo cambiar el peso de la rodilla

Editar `KNEE_SHARE` en `ObdxCompetitorEventScore` (hoy `0.85`). Los calificativos salen de la configuración
(`qualifications` en `configuration.json`) y la franja del grado de `ObdxConfigurationsRankThresholds`.
