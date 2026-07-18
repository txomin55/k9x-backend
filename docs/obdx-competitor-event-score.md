# OBDX · `competitorEventScore` (rank_score por competidor)

Cada competidor de una prueba OBDX obtiene su propio `rank_score` en `obdx.event_competitors.rank_score`
(`NUMERIC(6,2)`). Sustituye al viejo `floor + span·(total/max)`, que **sobre-premiaba ir a eventos con mucho
campo** (la banda del competidor se inflaba con el `rank_score` del evento). La nueva lógica **premia el
mérito**: hay que subir por los **calificativos** (qualifications) de la configuración.

## Idea

El competidor puede ganar entre `configBandMin − 1` (no llegó) y `eventScore` (perfecto). El reparto:

1. **No llega a la 1ª qualificación** → cae al **techo del rango anterior**: `configBandMin − 1`
   (p.ej. GRADE_3 → 600). "No te habías ganado estar en esta categoría".
2. **Llegar a la 1ª qualificación** desbloquea un **10 % fijo** de `span = eventScore − configBandMin`.
3. El **90 % restante** (la "ventana") tiene una **rodilla en el calificativo MÁS ALTO** que defina la config
   (dinámico: EXC, u otro si lo hubiera):
   - de la **1ª qualificación al top qualificativo** se gana el **85 %** de la ventana (ahí está el grueso);
   - del **top qualificativo al máximo** se gana el **15 %** restante (solo pulido).
   - Un 100 % de nota cae exactamente en `eventScore`.

La intuición de obediencia: **llegar a EXC (≈80%) ya es la excelencia**; del 80 % al 100 % solo estás puliendo.

## Fórmula

```
firstQual = min(qualifications) ; topQual = max(qualifications)
max       = maxAllowedScore · Σcoef       ; total = nota ponderada del competidor
span      = eventScore − configBandMin
window    = 0.90 · span                    ; unlock = 0.10 · span
base      = configBandMin + unlock         ; KNEE = 0.85

total < firstQual              → configBandMin − 1
firstQual ≤ total ≤ topQual    → base + (total−firstQual)/(topQual−firstQual) · (KNEE·window)
topQual  < total ≤ max         → base + KNEE·window + clamp((total−topQual)/(max−topQual)) · ((1−KNEE)·window)
```

Redondeo a 2 decimales (HALF_UP). Degenerados: **sin qualifications** → sin caída y la ventana sube lineal de
`0→max`; **una sola qualification** (`topQual == firstQual`) → ventana lineal de `firstQual→max` (sin rodilla).

## Ejemplo (FCI GRADE_3)

Evento `eventScore = 800`, franja `[601, 900]` → `configBandMin = 601`, `span = 199`. Calificativos
`B = 192`, `MB = 224`, `EXC = 256` (el top). `max = 320`. → `unlock = 19.9`, `window = 179.1`,
`base = 620.9`, `KNEE·window = 152.235`.

| total | % (total/max) | calificativo | competitorEventScore |
|---|---|---|---|
| < 192 | < 60% | — (no llega a B) | **600** |
| 192 | 60% | B | **620.90** |
| 224 | 70% | MB | 697.02 |
| 256 | 80% | **EXC (rodilla)** | **773.14** |
| 288 | 90% | — | 786.57 |
| 320 | 100% | — | **800.00** |

De 192→256 se ganan 152.24 puntos (el 85% de la ventana); de 256→320 solo 26.87 (el 15%, pulido).

## Dónde vive

| Pieza | Fichero |
|---|---|
| Fórmula | `k9x-backend-domain/.../disciplines/obdx/ObdxCompetitorEventScore.java` |
| Cálculo (total/max + calificativos + franja) | `k9x-backend-application/.../events/obdx/use_case/GetObdxClassificationServiceCase.java` (`competitorRankScore`) |
| Persistencia (cron transaccional) | `GenerateEventSnapshotsServiceCase` → `SaveObdxSnapshotJooqAdapter` |
| Columna | `obdx.event_competitors.rank_score` (`NUMERIC(6,2)`) |

## Cómo cambiar el peso de la rodilla

Editar `KNEE_SHARE` en `ObdxCompetitorEventScore` (hoy `0.85`). Los calificativos y la franja salen de la
configuración (`qualifications` en `configuration.json` y `ObdxConfigurationsRankThresholds`).
