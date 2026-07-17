# OBDX · Baremos de `rank_score` por configuración

Tabla de referencia: para cada configuración (su franja `[min, max]`), qué `rank_score` y qué letra `rank`
produce la fórmula según el **nº de competidores** (tier) y si el evento es **internacional**.

Recordatorio de la fórmula (`range = max - min`):

```
rank_score = min + round(tier/5 · 0.9·range) + (internacional ? round(0.1·range) : 0)
```

- **Tier por nº de competidores:** `E (<5)`, `D (5–9)`, `C (10–19)`, `B (20–34)`, `A (≥35)` → pesos `1..5`.
- **Internacional:** al menos un competidor de país distinto al de la competición → `+10 %` del range **y**
  sufijo `+`.
- **Letra (rango global del score):** `E ≤200`, `D 201–400`, `C 401–600`, `B 601–800`, `A 801–1000`.
- **Tope automático 950**; `951–1000` reservado a `A++` **manual** (no lo produce la fórmula).

> En cada tabla, cada celda es `rank_score (rank)`.

---

## CPC_COBS · OBDX_RSCE_DEBUTANTE — franja `[100, 200]` (range 100)

Toda la franja cae en el rango global **E**, así que la letra es siempre `E`/`E+`; el nº de competidores solo
mueve el número.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (E) | 118 (E) | 128 (E+) |
| 5–9 (D) | 136 (E) | 146 (E+) |
| 10–19 (C) | 154 (E) | 164 (E+) |
| 20–34 (B) | 172 (E) | 182 (E+) |
| ≥ 35 (A) | 190 (E) | 200 (E+) |

---

## OBDX_FCI_GRADE_1 · OBDX_RSCE_GRADE_1 — franja `[201, 400]` (range 199)

Toda la franja cae en el rango global **D** → letra siempre `D`/`D+`.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (E) | 237 (D) | 257 (D+) |
| 5–9 (D) | 273 (D) | 293 (D+) |
| 10–19 (C) | 308 (D) | 328 (D+) |
| 20–34 (B) | 344 (D) | 364 (D+) |
| ≥ 35 (A) | 380 (D) | 400 (D+) |

---

## OBDX_FCI_GRADE_2 — franja `[401, 600]` (range 199)

Toda la franja cae en el rango global **C** → letra siempre `C`/`C+`.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (E) | 437 (C) | 457 (C+) |
| 5–9 (D) | 473 (C) | 493 (C+) |
| 10–19 (C) | 508 (C) | 528 (C+) |
| 20–34 (B) | 544 (C) | 564 (C+) |
| ≥ 35 (A) | 580 (C) | 600 (C+) |

---

## OBDX_FCI_GRADE_3 — franja `[601, 950]` (range 349)

Es la única franja que **cruza de B a A** (frontera en 800): con pocos competidores da `B`, y a partir de
cierto punto (o con internacional) sube a `A`. El máximo automático es **950** (`A+`); `A++` solo manual.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (E) | 664 (B) | 699 (B+) |
| 5–9 (D) | 727 (B) | 762 (B+) |
| 10–19 (C) | 789 (B) | 824 (A+) |
| 20–34 (B) | 852 (A) | 887 (A+) |
| ≥ 35 (A) | 915 (A) | 950 (A+) |

---

## Resumen de letra por configuración

| Configuración | Franja | Letra(s) posibles |
|---|---|---|
| CPC_COBS, OBDX_RSCE_DEBUTANTE | 100–200 | E / E+ |
| OBDX_FCI_GRADE_1, OBDX_RSCE_GRADE_1 | 201–400 | D / D+ |
| OBDX_FCI_GRADE_2 | 401–600 | C / C+ |
| OBDX_FCI_GRADE_3 | 601–950 | B / B+ / A / A+ |
| — (manual, seed) | 951–1000 | A++ |
