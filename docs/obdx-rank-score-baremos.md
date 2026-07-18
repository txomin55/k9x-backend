# OBDX · Baremos de `rank_score` por configuración

Tabla de referencia: para cada configuración (su franja `[min, max]`), qué `rank_score` y qué letra `rank`
produce la fórmula según el **nº de competidores** (un *tier* 1–5) y si el evento es **internacional**.

Recordatorio de la fórmula (`ObdxConfigurationsRankThresholds.eventScore`, `range = max - min`):

```
rank_score = min + round(tier/5 · 0.9·range) + (internacional ? round(0.1·range) : 0)
```

- **Tier por nº de competidores (1–5):** `<5 → 1`, `5–9 → 2`, `10–19 → 3`, `20–34 → 4`, `≥35 → 5`.
  Es solo una **capa** para posicionar dentro de la franja; **no** es la letra `rank`.
- **Internacional:** al menos un competidor de país distinto al de la competición → `+10 %` del range **y**
  sufijo `+`.
- **Letra (rango global del score):** `E ≤200`, `D 201–400`, `C 401–600`, `B 601–800`, `A 801–900`,
  `S 901–1000`.
- **Tope automático 900**; `901–1000` es la letra `S`, **manual** y siempre internacional (`S+`), nunca la
  produce la fórmula.

> En cada tabla, cada celda es `rank_score (rank)`.

---

## CPC_COBS · OBDX_RSCE_DEBUTANTE — franja `[100, 200]` (range 100)

Toda la franja cae en el rango global **E**, así que la letra es siempre `E`/`E+`; el nº de competidores solo
mueve el número.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (1) | 118 (E) | 128 (E+) |
| 5–9 (2) | 136 (E) | 146 (E+) |
| 10–19 (3) | 154 (E) | 164 (E+) |
| 20–34 (4) | 172 (E) | 182 (E+) |
| ≥ 35 (5) | 190 (E) | 200 (E+) |

---

## OBDX_FCI_GRADE_1 · OBDX_RSCE_GRADE_1 — franja `[201, 400]` (range 199)

Toda la franja cae en el rango global **D** → letra siempre `D`/`D+`.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (1) | 237 (D) | 257 (D+) |
| 5–9 (2) | 273 (D) | 293 (D+) |
| 10–19 (3) | 308 (D) | 328 (D+) |
| 20–34 (4) | 344 (D) | 364 (D+) |
| ≥ 35 (5) | 380 (D) | 400 (D+) |

---

## OBDX_FCI_GRADE_2 — franja `[401, 600]` (range 199)

Toda la franja cae en el rango global **C** → letra siempre `C`/`C+`.

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (1) | 437 (C) | 457 (C+) |
| 5–9 (2) | 473 (C) | 493 (C+) |
| 10–19 (3) | 508 (C) | 528 (C+) |
| 20–34 (4) | 544 (C) | 564 (C+) |
| ≥ 35 (5) | 580 (C) | 600 (C+) |

---

## OBDX_FCI_GRADE_3 — franja `[601, 900]` (range 299)

Es la única franja que **cruza de B a A** (frontera en 800): con pocos competidores da `B`, y a partir del
tier 4 sube a `A`. El máximo automático es **900** (`A+`); **nunca llega a `S`** (901–1000, manual).

| Nº competidores (tier) | Nacional | Internacional |
|---|---|---|
| < 5 (1) | 655 (B) | 685 (B+) |
| 5–9 (2) | 709 (B) | 739 (B+) |
| 10–19 (3) | 762 (B) | 792 (B+) |
| 20–34 (4) | 816 (A) | 846 (A+) |
| ≥ 35 (5) | 870 (A) | 900 (A+) |

---

## Resumen de letra por configuración

| Configuración | Franja | Letra(s) posibles |
|---|---|---|
| CPC_COBS, OBDX_RSCE_DEBUTANTE | 100–200 | E / E+ |
| OBDX_FCI_GRADE_1, OBDX_RSCE_GRADE_1 | 201–400 | D / D+ |
| OBDX_FCI_GRADE_2 | 401–600 | C / C+ |
| OBDX_FCI_GRADE_3 | 601–900 | B / B+ / A / A+ |
| — (manual, seed) | 901–1000 | S / S+ |
