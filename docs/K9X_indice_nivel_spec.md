# Sistema de Índice de Competidor — K9X / OBDX

Especificación de cómo combinar las **puntuaciones que un competidor obtiene en cada evento** (`puntuacionEventoCompetidor`) a lo largo del tiempo en un único **índice**, aplicando ponderación por antigüedad (nivel) y degradación por inactividad (frescura).

> **Alcance / caja negra.** Este documento trata **solo** de la degradación y la media de las **puntuaciones que un competidor obtiene en cada evento** a lo largo del tiempo. Cómo se calcula cada una de esas puntuaciones —incluyendo el valor del evento y el rendimiento bruto del competidor— es una **caja negra** externa y queda fuera de alcance. Aquí se recibe, por cada resultado, un número ya calculado: `puntuacionEventoCompetidor` (la puntuación del competidor en ese evento, **no** el valor del evento).

---

## 1. Qué es el índice (y qué NO es)

El índice mide el **standing actual** de un competidor: su nivel demostrado, **ponderado por lo actual que sea**. Se compone de dos factores:

```
índice = nivel × frescura
```

- **nivel** — el nivel sostenido que el competidor demuestra (media de las puntuaciones que obtiene en cada evento, ponderada por antigüedad). Es **estable**: no se degrada por el simple paso del tiempo.
- **frescura** — cómo de reciente es su última competición (0.01–1.0). Vale 1.0 si compite ahora y **decae si está inactivo**.

Objetivo explícito: **el índice premia a los competidores actuales, no glorifica viejas glorias.** Un competidor que dejó de competir se hunde en el ranking; cuando vuelve y compite, su frescura salta a 1.0 y recupera su nivel.

Definición elegida entre cuatro posibles, para que quede documentado:

| Interpretación | ¿Es esto el índice? |
|---|---|
| Techo — lo mejor que ha hecho | No |
| Foto actual — su nivel exacto de hoy, sin historial | No |
| Promedio sostenido puro (sin factor de frescura) | No (glorifica inactivos) |
| **Nivel sostenido × frescura (standing actual)** | **Sí ← elegido** |

**Consecuencias de esta elección (esperadas, no bugs):**
- El **nivel** sube y baja despacio: va por detrás de la realidad a propósito, para no reaccionar a un solo resultado.
- La **frescura** hunde a los inactivos hacia casi cero (× 0.01 tras ~6 años sin competir), pero **es recuperable**: en cuanto vuelve a competir, vuelve a 1.0.
- Un competidor con **una sola prueba** tiene nivel = esa puntuación, y su índice se degrada por la frescura conforme envejece esa prueba.
- Si quieres mostrar además "está por debajo de su mejor forma", eso es un **indicador de forma** aparte (ver §8), no el índice.

---

## 2. Entrada

Por cada competidor, una lista de resultados. Cada resultado:

| Dato | Descripción | Origen |
|---|---|---|
| `fecha` | Fecha del resultado | Dato |
| `puntuacionEventoCompetidor` | Puntuación que **el competidor** obtuvo en ese evento, **ya calculada** (no el valor del evento) | **Caja negra externa** |

No se necesita nada más: ni la puntuación bruta, ni el valor del evento, ni el grado, ni ninguna tabla de conversión. Todo eso vive en la capa de puntuación (caja negra) y no es asunto de este sistema.

---

## 3. Curvas de peso por antigüedad

Hay **dos curvas** distintas, porque cumplen funciones distintas (ver §4). Ambas: meseta plana de 10 meses a peso 1, rampa lineal descendente, y suelo permanente de 0.01 (nada llega nunca a 0).

### 3.1 Curva de NIVEL — pondera unos resultados frente a otros

```
pesoNivel(a) = 1.0                   si  a ≤ 10 meses
pesoNivel(a) = interpolación lineal   si  10 < a ≤ 70 meses
               anclajes:  10→1.0  22→0.75  34→0.5  46→0.25  58→0.1  70→0.01
pesoNivel(a) = 0.01                  si  a > 70 meses
```

### 3.2 Curva de FRESCURA — degrada el índice del inactivo

Igual que la de nivel pero con **caída inicial más pronunciada** al salir de la meseta (10→18), reincorporándose a la curva de nivel a partir del mes 18. Esto suaviza el tramo 12–24, que resultaba un salto grande.

```
frescura(a) = 1.0                    si  a ≤ 10 meses
frescura(a) = interpolación lineal    si  10 < a ≤ 70 meses
              anclajes:  10→1.0  12→0.92  18→0.84  22→0.75  34→0.5  46→0.25  58→0.1  70→0.01
frescura(a) = 0.01                   si  a > 70 meses
```

Efecto sobre un competidor de nivel 700 según meses sin competir: 700 (mes 10) → 644 (12) → 588 (18) → 496 (24) → 350 (34) → 175 (46) → 70 (58) → 7 (70).

**Notas de diseño:**
- Meseta de 10 meses: lo del último año cuenta pleno.
- El suelo de 0.01 (> 0) garantiza que **ningún competidor desaparece**.
- La curva de frescura solo difiere de la de nivel en el tramo 10–18 (arranque más brusco); desde el mes 18 son idénticas.

---

## 4. Índice = nivel × frescura

El índice combina dos factores, cada uno con su **propia** curva de peso (§3).

**Nivel** — media de las puntuaciones del competidor ponderada con la curva de nivel (§3.1). Estable; con una sola prueba, el peso se cancela y el nivel = esa puntuación:

```
nivel = Σ(pesoNivel_i × puntuacionEventoCompetidor_i) / Σ(pesoNivel_i)
```

**Frescura** — la curva de frescura (§3.2) evaluada en la antigüedad de la prueba **más reciente** del competidor (1.0 si compite ahora, decae si está inactivo):

```
frescura = frescura(antigüedad de la prueba más reciente)
```

**Índice final:**

```
índice = nivel × frescura
```

Redondeo final: `BigDecimal` con `RoundingMode.HALF_UP`.

> Las dos curvas cumplen funciones distintas: la de **nivel** pondera unas pruebas frente a otras (relativo → se cancela con una sola prueba); la de **frescura** escala el índice entero según lo reciente que sea la última competición (absoluto → sí degrada al inactivo, incluso con una sola prueba). Solo difieren en el tramo 10–18 meses.

Se recomienda exponer `nivel`, `frescura` e `índice` por separado, para que la UI pueda mostrar el nivel real del competidor junto a lo actual que es.

---

## 5. Reglas de estado

| Situación | Regla |
|---|---|
| **Provisional** | Menos de **2** pruebas → el competidor **SÍ tiene índice**, pero se marca como provisional (estilo rating provisional del ajedrez). No desaparece. |
| **Efecto suerte** | Se neutraliza solo: con ≥2 pruebas el promedio diluye un resultado afortunado. No hacen falta anclajes ni fórmulas extra. |
| **Inactividad / lesión** | La **frescura** degrada el índice mientras no compite (nivel × frescura decreciente). **Es recuperable**: al volver a competir, la frescura salta a 1.0 y el índice vuelve al nivel. Un lesionado baja en el ranking temporalmente, no de forma permanente. |
| **Nadie desaparece** | El suelo de 0.01 (> 0) garantiza que todo competidor con al menos una prueba conserva un índice, por pequeño que sea. |
| **Viejas glorias** | Un competidor que dejó de competir se hunde hacia casi cero por la frescura. El índice premia a los **actuales**, no el pasado. |

> **Persistencia.** El flag **provisional** no se persiste: se calcula **al vuelo en lectura**, contando las
> filas del historial del perro en `k9x.snap_dog_rank` (`provisional = filas < 2`). No hay columna para él.

---

## 6. Decisiones descartadas (para no volver a ellas)

Todas referidas a la capa de degradación/media (la única de la que trata este spec):

| Alternativa | Por qué se descartó |
|---|---|
| Media simple de toda la carrera | Un competidor de 10 años quedaría aplastado por resultados antiguos irrelevantes. |
| Quedarse con la última prueba | Un mal día puntual hundiría el índice sin justificación. |
| Shrinkage bayesiano (anclas `k`, `C`) | Sobrecomplejo. El problema que resolvía (efecto suerte) lo cubre la regla de "provisional con <2 pruebas". |
| Coger las "mejores N" pruebas (subset) | **Amplifica** el efecto suerte: el resultado afortunado es justo el que entra. |
| Penalización explícita/sustractiva por inactividad (restar N puntos) | Doble castigo y no recuperable. En su lugar se usa la **frescura multiplicativa** (§4): degrada al inactivo pero se recupera al volver a competir. |

---

## 7. Parámetros (resumen para calibrar)

| Parámetro | Valor actual | Nota |
|---|---|---|
| Meseta (peso pleno) | 10 meses | Igual en ambas curvas |
| Anclajes NIVEL | 10→1.0, 22→0.75, 34→0.5, 46→0.25, 58→0.1, 70→0.01 | Pondera resultados entre sí |
| Anclajes FRESCURA | 10→1.0, 12→0.92, 18→0.84, 22→0.75, 34→0.5, 46→0.25, 58→0.1, 70→0.01 | Arranque más brusco (10–18); desde el 18 = curva de nivel |
| Suelo de peso | 0.01 permanente (desde el mes 70) | > 0, garantiza que nadie desaparece |
| Umbral provisional | 2 pruebas | <2 → marcado, pero con índice |

---

## 8. Extensión futura opcional — Indicador de forma

Si quieres mostrar que un competidor está **por debajo de su mejor versión** (aunque su nivel siga alto), es un **segundo número** independiente del índice:

```
forma = (media reciente de puntuaciones del competidor − media histórica) / media histórica
```

Se muestra al lado del índice (ej. "forma: −4%"). No modifica el índice; lo complementa.

> **Persistencia.** Igual que el flag provisional, la **forma** no se persiste: se calcula **al vuelo en
> lectura** a partir del historial de `k9x.snap_dog_rank` (medias reciente e histórica). No hay columna para ella.

---

## 9. Implementación de referencia (Java)

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class IndiceCalculator {

    private static final double MESETA_MESES = 10.0;
    private static final double SUELO_PESO = 0.01;
    private static final int UMBRAL_PROVISIONAL = 2;

    // Curva de NIVEL: pondera unos resultados frente a otros
    private static final double[][] ANCLAJES_NIVEL = {
        {10, 1.00}, {22, 0.75}, {34, 0.50}, {46, 0.25}, {58, 0.10}, {70, 0.01}
    };

    // Curva de FRESCURA: arranque más brusco (10–18); desde el mes 18 = curva de nivel
    private static final double[][] ANCLAJES_FRESCURA = {
        {10, 1.00}, {12, 0.92}, {18, 0.84}, {22, 0.75},
        {34, 0.50}, {46, 0.25}, {58, 0.10}, {70, 0.01}
    };

    /** puntuacionEventoCompetidor es caja negra: llega ya calculada por la capa de puntuación. */
    public record Resultado(LocalDate fecha, BigDecimal puntuacionEventoCompetidor) {}
    public record Indice(BigDecimal valor, BigDecimal nivel, BigDecimal frescura,
                         boolean provisional, int numPruebas) {}

    public Indice calcular(List<Resultado> resultados, LocalDate hoy) {
        if (resultados.isEmpty()) {
            return new Indice(null, null, null, true, 0);
        }

        BigDecimal numerador = BigDecimal.ZERO;
        BigDecimal denominador = BigDecimal.ZERO;
        double mesesMasReciente = Double.MAX_VALUE;

        for (Resultado r : resultados) {
            double meses = ChronoUnit.DAYS.between(r.fecha(), hoy) / 30.4375;
            BigDecimal peso = BigDecimal.valueOf(interpolar(meses, ANCLAJES_NIVEL));
            numerador = numerador.add(peso.multiply(r.puntuacionEventoCompetidor()));
            denominador = denominador.add(peso);
            mesesMasReciente = Math.min(mesesMasReciente, meses);
        }

        // Nivel: media ponderada estable (el peso se cancela con una sola prueba)
        BigDecimal nivel = numerador.divide(denominador, 4, RoundingMode.HALF_UP);

        // Frescura: curva propia evaluada en la prueba más reciente (recuperable)
        BigDecimal frescura = BigDecimal.valueOf(interpolar(mesesMasReciente, ANCLAJES_FRESCURA));

        // Índice final: nivel × frescura
        BigDecimal indice = nivel.multiply(frescura).setScale(2, RoundingMode.HALF_UP);

        boolean provisional = resultados.size() < UMBRAL_PROVISIONAL;
        return new Indice(indice, nivel.setScale(2, RoundingMode.HALF_UP),
                          frescura, provisional, resultados.size());
    }

    /** Interpolación lineal sobre una curva de anclajes: meseta, rampa, suelo. */
    private double interpolar(double meses, double[][] anclajes) {
        if (meses <= MESETA_MESES) return 1.0;
        double[] ultimo = anclajes[anclajes.length - 1];
        if (meses >= ultimo[0]) return SUELO_PESO;
        for (int i = 0; i < anclajes.length - 1; i++) {
            double m0 = anclajes[i][0],  w0 = anclajes[i][1];
            double m1 = anclajes[i + 1][0], w1 = anclajes[i + 1][1];
            if (meses >= m0 && meses <= m1) {
                double t = (meses - m0) / (m1 - m0);
                return w0 + t * (w1 - w0);
            }
        }
        return SUELO_PESO;
    }
}
```

### Nota sobre el ranking

Sobre los índices ya calculados, ordenar de mayor a menor con una cadena de `Comparator` y asignar **ranking estándar de competición** (1, 2, 2, 4 — no dense ranking). Los competidores provisionales pueden mostrarse aparte o marcados con asterisco, según prefieras en la UI.

---

## 10. Implementación en K9X: tablas snap y doble timestamp

El índice está materializado sobre una cadena de tablas `snap_*` (escritas **solo** por crons, append-only,
inserts idempotentes):

| Tabla | Qué guarda | Quién la escribe |
|---|---|---|
| `obdx.snap_event_competitors_results` | La foto del evento: position, total_score, rank_score por competidor | Cron diario de snapshot |
| `k9x.snap_dog_rank` | El historial crudo del perro: su rank_score por evento y disciplina (el insumo del índice) | Cron diario, misma transacción |
| `obdx.snap_event_classification` | El JSON de la clasificación + el marcador de "evento ya congelado" | Cron diario, misma transacción |
| `k9x.snap_dog_index_history` | La línea temporal del índice por (perro, disciplina): registros `EVENT` y `TIME_DEGRADATION` con metadata JSON | Cron quincenal |

Toda tabla snap lleva **dos timestamps** con contrato fijo:

- **`timestamp`** — el instante de la **persistencia**. Solo auditoría: nadie lo usa para calcular nada.
- **`applying_timestamp`** — el instante **al que aplica el dato**: el fin de la etapa del evento
  (`stages.date_to`) para todo lo derivado de un evento, o el momento de la evaluación en los registros de
  degradación. El índice, la frescura, el orden temporal y el "qué es nuevo" usan **solo** este campo.

Gracias a esa separación, snapshotear hoy un evento de hace años produce historia correcta: el dato queda
fechado en su momento real, no en el de la ingesta.

---

## 11. Ingesta de eventos históricos (backfill)

Para añadir una prueba pasada (p. ej. eventos previos al Trofeu) **no se toca ninguna tabla snap**; basta con
crear el evento como si fuera real:

1. `k9x.competitions` + `k9x.stages` con las **fechas reales** de la prueba (`date_to` será el
   `applying_timestamp` de todo lo derivado).
2. `k9x.events`: `discipline`, un `configuration_id` **existente en código** (los `configuration.json` de
   `.../disciplines/obdx/federations/` y las franjas de `ObdxConfigurationsRankThresholds` — no hay tablas de
   configuración) y **⚠️ `rank_score` + `international` puestos a mano**: esa fórmula corre al guardar el
   evento por la API, no en el cron. Sin `rank_score`, los competidores no puntúan y el evento no afecta al
   índice.
3. `obdx.event_judges`, `obdx.event_exercises` (con los jueces asignados por ejercicio: la clasificación solo
   lee scores de pares juez+ejercicio listados ahí), `obdx.event_competitors` y `obdx.event_scores`.

A partir de ahí, todo es automático: el cron diario ve la etapa terminada sin marcador y congela el evento
(`timestamp` = hoy, `applying_timestamp` = fecha real), y el cron quincenal integra los resultados — registro
`EVENT` en la fecha real, nivel recalculado con la antigüedad real y degradación contada desde la última
prueba real del perro.

**Dos matices:**

- **La historia ya escrita no se reescribe.** Si un perro ya tiene registros en `snap_dog_index_history` y se
  ingesta un evento *anterior* a ellos, el evento sí entra en `snap_dog_rank` y pondera en todos los cálculos
  futuros, pero su punto `EVENT` no se materializa (el replay salta lo anterior al último registro) y los
  registros pasados conservan su valor. Solución: **borrar las filas de `snap_dog_index_history` de los perros
  afectados** — el replay es idempotente y la siguiente pasada del cron reconstruye la línea temporal completa
  ya con el evento antiguo en su sitio. Orden ideal: backfill primero, historia después.
- **Los meses de degradación intermedios no se materializan retroactivamente**: tras el backfill, el primer
  registro `TIME_DEGRADATION` salta directamente al mes de inactividad actual (valor correcto; sin puntos
  intermedios en la gráfica).

---

## Resumen en una frase

> El índice es **nivel × frescura**, calculado sobre las `puntuacionEventoCompetidor` (caja negra) de un competidor: el *nivel* es su media ponderada por antigüedad (meseta de 10 meses, decayendo hasta 0.01 en el mes 70); la *frescura* es una curva propia evaluada en la prueba más reciente, con arranque algo más brusco (10–18 meses), que hunde a los inactivos hacia casi cero pero se recupera al volver a competir. Con menos de 2 pruebas se marca provisional, y nadie desaparece nunca.
