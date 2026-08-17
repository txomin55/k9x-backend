# OBDX · Baremos de `rank_score` por configuración

Tabla de referencia: para cada configuración y **categoría**, qué `rank_score` y qué letra `rank` produce la
fórmula según el **nº de competidores** (un *tier* 1–3).

Recordatorio de la fórmula (`ObdxConfigurationsRankThresholds.eventScore`), con `[subMin, subMax]` la
sub-banda de la categoría y `range = subMax - subMin`:

```
rank_score = subMin + round(tier/3 · range)
```

- **Tier por nº de competidores (1–3):** `<10 → 1`, `10–24 → 2`, `≥25 → 3`. Es solo una **capa** para
  posicionar dentro de la sub-banda; **no** es la letra `rank`.
- **Categorías:** `CLUB` toma los 3/4 bajos de la franja y `OPEN` el cuarto alto. **Solo GRADE_3** admite
  `WC_Q`, `WC_SEMI` y `WC_FINAL`. `WC_Q` tiene sub-banda propia `[775, 850]`, así que sí escala por tier
  (800 / 825 / 850); `WC_SEMI` y `WC_FINAL` son **puntos fijos** (900 / 1000): no dependen del nº de
  competidores.
- **Letra (rango global del score):** `E ≤200`, `D 201–400`, `C 401–600`, `B 601–800`, `A 801–900`,
  `S 901–1000`.
- No hay flag `international` ni sufijo `+`: la letra es siempre una letra sola.

> En cada tabla, cada celda es `rank_score (rank)`.

---

## OBDX_ENCI_PREDEBUTTANTI — franja `[50, 100]`

Sub-bandas: `CLUB [50, 88]`, `OPEN [89, 100]`. Toda la franja cae en el rango global **E**.

| Nº competidores (tier) | CLUB | OPEN |
|---|---|---|
| < 10 (1) | 63 (E) | 93 (E) |
| 10–24 (2) | 75 (E) | 96 (E) |
| ≥ 25 (3) | 88 (E) | 100 (E) |

---

## OBDX_ENCI_DEBUTTANTI · CPC_COBS · OBDX_RSCE_DEBUTANTE — franja `[100, 200]`

Sub-bandas: `CLUB [100, 175]`, `OPEN [176, 200]`. Toda la franja cae en el rango global **E**.

| Nº competidores (tier) | CLUB | OPEN |
|---|---|---|
| < 10 (1) | 125 (E) | 184 (E) |
| 10–24 (2) | 150 (E) | 192 (E) |
| ≥ 25 (3) | 175 (E) | 200 (E) |

---

## OBDX_FCI_GRADE_1 · OBDX_RSCE_GRADO_1 — franja `[201, 400]`

Sub-bandas: `CLUB [201, 350]`, `OPEN [351, 400]`. Toda la franja cae en el rango global **D**.

| Nº competidores (tier) | CLUB | OPEN |
|---|---|---|
| < 10 (1) | 251 (D) | 367 (D) |
| 10–24 (2) | 300 (D) | 384 (D) |
| ≥ 25 (3) | 350 (D) | 400 (D) |

---

## OBDX_FCI_GRADE_2 — franja `[401, 600]`

Sub-bandas: `CLUB [401, 550]`, `OPEN [551, 600]`. Toda la franja cae en el rango global **C**.

| Nº competidores (tier) | CLUB | OPEN |
|---|---|---|
| < 10 (1) | 451 (C) | 567 (C) |
| 10–24 (2) | 500 (C) | 584 (C) |
| ≥ 25 (3) | 550 (C) | 600 (C) |

---

## OBDX_FCI_GRADE_3 — franja `[601, 1000]`

La única configuración que admite las categorías de mundial, y la única que cruza de **B** a **A** y a **S**.
La clasificatoria escala por tier dentro de `[775, 850]` (800 / 825 / 850); la semifinal y la final son puntos
fijos: da igual cuánta gente se presente a una final, vale 1000.

| Nº competidores (tier) | CLUB | OPEN | WC_Q | WC_SEMI | WC_FINAL |
|---|---|---|---|---|---|
| < 10 (1) | 634 (B) | 717 (B) | 800 (B) | 900 (A) | 1000 (S) |
| 10–24 (2) | 667 (B) | 734 (B) | 825 (A) | 900 (A) | 1000 (S) |
| ≥ 25 (3) | 700 (B) | 750 (B) | 850 (A) | 900 (A) | 1000 (S) |

Sub-bandas: `CLUB [601, 700]`, `OPEN [701, 750]`, `WC_Q [775, 850]`, `WC_SEMI = 900`, `WC_FINAL = 1000`. Los
tramos 751–774, 851–899 y 901–999 quedan vacíos a propósito (el suelo 775 de `WC_Q` tampoco es alcanzable:
el tier nunca cae en el suelo de su sub-banda, así que la clasificatoria más pequeña ya vale 800). `WC_Q` es
la única categoría que cruza la frontera B/A: con menos de 10 competidores se queda en **B**.

---

## Resumen de letra por configuración

| Configuración | Franja | Categorías | Letra(s) posibles |
|---|---|---|---|
| OBDX_ENCI_PREDEBUTTANTI | 50–100 | CLUB, OPEN | E |
| OBDX_ENCI_DEBUTTANTI, CPC_COBS, OBDX_RSCE_DEBUTANTE | 100–200 | CLUB, OPEN | E |
| OBDX_FCI_GRADE_1, OBDX_RSCE_GRADO_1 | 201–400 | CLUB, OPEN | D |
| OBDX_FCI_GRADE_2 | 401–600 | CLUB, OPEN | C |
| OBDX_FCI_GRADE_3 | 601–1000 | las cinco | B / A / S |
