# Sistema de Índice de Competidor — K9X / OBDX

Especificación de cómo combinar las **puntuaciones que un competidor obtiene en cada evento** (`puntuacionEventoCompetidor`) a lo largo del tiempo en un único **índice**, aplicando ponderación por antigüedad (nivel) y degradación por inactividad (frescura).

> **Alcance / caja negra.** Este documento trata **solo** de la degradación y la media de las **puntuaciones que un competidor obtiene en cada evento** a lo largo del tiempo. Cómo se calcula cada una de esas puntuaciones —incluyendo el valor del evento y el rendimiento bruto del competidor— es una **caja negra** externa y queda fuera de alcance. Aquí se recibe, por cada resultado, un número ya calculado: `puntuacionEventoCompetidor` (la puntuación del competidor en ese evento, **no** el valor del evento).

---

## 1. Qué es el índice (y qué NO es)

El índice mide el **standing actual** de un competidor: su nivel demostrado, **ponderado por lo actual que sea**. Se compone de dos factores:

```
índice = nivel × frescura
```

- **nivel** — el nivel sostenido que el competidor demuestra: la media de sus **N mejores resultados** (ponderados por antigüedad) sobre un **denominador fijo N**. Es **estable** y **monótono**: competir nunca lo baja.
- **frescura** — cómo de reciente es su última competición (0.01–1.0). Vale 1.0 si compite ahora y **decae si está inactivo**.

Objetivo explícito: **el índice premia a los competidores actuales, no glorifica viejas glorias.** Un competidor que dejó de competir se hunde en el ranking; cuando vuelve y compite, su frescura salta a 1.0 y recupera su nivel.

Definición elegida entre cuatro posibles, para que quede documentado:

| Interpretación | ¿Es esto el índice? |
|---|---|
| Techo — lo mejor que ha hecho | No |
| Foto actual — su nivel exacto de hoy, sin historial | No |
| Promedio sostenido puro (sin factor de frescura) | No (glorifica inactivos) |
| **Nivel sostenido × frescura (standing actual)** | **Sí ← elegido** |

**Las tres propiedades que el modelo garantiza** (y de las que salen todas las decisiones de §3 y §4):

1. **Competir nunca baja el índice.** Un resultado peor de lo habitual no entra entre los N mejores, así que no resta; y además refresca la frescura a 1.0. Un perro que gana el mundial puede ir a pruebas menores cada 3 meses sin que su rank caiga por ello. Lo único que baja el índice es **el tiempo**.
2. **Un resultado excepcional no te define.** Con denominador fijo N, una sola prueba de 950 aporta `950/N` al nivel: para valer 950 hay que demostrarlo N veces. El índice no deriva hacia tu última puntuación.
3. **Nadie desaparece, y volver siempre recupera.** El suelo de peso 0.01 mantiene un índice a todo el que haya competido alguna vez, y la frescura es multiplicativa y recuperable, no una penalización sustractiva.

**Consecuencias esperadas (no son bugs):**
- El nivel sube **por escalones** conforme se llenan las N plazas, no de forma continua: un perro con 1 prueba de 750 marca 384, con 2 marca 567, con 3 marca 750 (§4.1.1). Es la rampa de entrada, y es la razón por la que existe el flag *provisional* (§5).
- Un competidor puede tener un índice **por debajo** de lo que su mejor prueba sugiere durante su primera temporada. Es intencionado.
- Si quieres mostrar además "está por debajo de su mejor forma", eso es un **indicador de forma** aparte (§8), no el índice.

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

Hay **dos curvas** distintas, porque cumplen funciones distintas (ver §4). Ambas: meseta plana a peso 1, rampa lineal descendente por anclajes, y suelo permanente de 0.01 (nada llega nunca a 0).

### 3.0 Calibración: la carrera de un perro, no la de un humano

Un perro empieza a competir con **2-3 años** y se retira con **8-10**: la carrera deportiva completa son **60-96 meses**. Por eso los horizontes son cortos:

- Un resultado de hace **4 años** no es "antiguo": es de otra etapa de la vida del perro. Al mes 44 el peso ya es residual (0.05).
- Un parón de **1 año** es ~15 % de la carrera — el equivalente a 2-3 años en un atleta humano. La frescura tiene que morder ya al año (−43 %).
- El retiro debe hundir al perro en **2-3 años**, no en 6.
- Las dos mesetas se calibran contra la **cadencia real de competición** (2-4 pruebas al año): la de nivel (8) cubre el tiempo de acumular las N=3 plazas, y la de frescura (6) el hueco normal entre pruebas. Acortarlas más introduce "diente de sierra": el índice de un perro que compite con normalidad oscilaría entre pruebas y el ranking se reordenaría sin que nadie haya rendido distinto.

### 3.1 Curva de NIVEL — pondera unos resultados frente a otros

```
pesoNivel(a) = 1.0                   si  a ≤ 8 meses
pesoNivel(a) = interpolación lineal   si  8 < a < 56 meses
               anclajes:  8→1.0  14→0.85  20→0.65  26→0.45  32→0.25  44→0.05  56→0.01
pesoNivel(a) = 0.01                  si  a ≥ 56 meses
```

Meseta de 8 meses: **el grueso de una temporada cuenta pleno**, y una prueba de la temporada anterior ya vale
menos que una de esta (al año, 0.90; a los dos años, 0.52).

### 3.2 Curva de FRESCURA — degrada el índice del inactivo

Meseta más corta (6 meses: el hueco normal entre pruebas de una misma temporada) y caída bastante más agresiva, para que un parón de un año se note.

```
frescura(a) = 1.0                    si  a ≤ 6 meses
frescura(a) = interpolación lineal    si  6 < a < 58 meses
              anclajes:  6→1.0  10→0.80  16→0.60  22→0.40  28→0.25  34→0.12  46→0.03  58→0.01
frescura(a) = 0.01                   si  a ≥ 58 meses
```

**Índice de un perro de nivel 750 según lo que lleve sin competir** (aquí ya está la caída completa: la frescura
más la pérdida de peso de sus resultados):

| Parón | 4 m | 6 m | 9 m | 12 m | 18 m | 24 m | 36 m |
|---|---|---|---|---|---|---|---|
| Índice | 725 | 700 | 553 | 428 | 233 | 102 | 14 |

Medio año parado cuesta un 7 %; un año, un 43 %; dos años te saca de la conversación. Coherente con una carrera
de 5-8 años (§3.0).

---

## 4. El cálculo

### 4.1 Nivel — media de las N mejores sobre denominador fijo

```
N = 3

candidatos    = todos los resultados del competidor, con  contribución_i = pesoNivel(edad_i) × puntuación_i
relleno       = min(C, mayor contribución del competidor)          ← plaza "por defecto", ver 4.1.1
seleccionados = las N mayores de  [contribuciones  ∪  N copias del relleno]
nivel         = Σ(seleccionados) / N                               ← denominador SIEMPRE N
```

Tres cosas importantes:

- **No hay ventana dura.** Se miran todos los resultados de la vida del perro; la curva de peso es la ventana, pero blanda. Un 950 de hace 4 años contribuye `0.04 × 950 ≈ 38`, así que no le gana la plaza a ningún resultado reciente. Ventaja frente a una ventana de corte seco (p. ej. "últimos 36 meses"): no hay acantilados el día que un resultado sale de la ventana.
- **La selección es por contribución, no por puntuación bruta.** Si se seleccionara por puntuación, un competidor con tres resultados iguales acabaría eligiendo los más viejos (los de menor peso) y su índice bajaría con el tiempo sin motivo.
- **El denominador es N, no la suma de pesos.** De aquí salen las propiedades 1 y 2 de §1: añadir un resultado solo puede aumentar el numerador (o dejarlo igual) → el índice **nunca baja al competir**; y un resultado suelto vale `1/N` del nivel → un pico no te define.

#### 4.1.1 El relleno de las plazas vacías (`C`)

El denominador es siempre N, así que un competidor con menos de N resultados tiene plazas sin ocupar. Contarlas
como **0** dejaría a un debutante con un 750 en un nivel de 250, que no es informativo. En su lugar cada plaza
vacía vale **`min(C, la mayor contribución del propio competidor)`**, con:

```
C = ObdxConfigurationsRankThresholds.FCI_GRADE_1.min() = 201
```

Es decir: **se te presupone el nivel de una prueba básica de grado 1 y, por encima de eso, hay que demostrarlo N
veces.** El `min(...)` es esencial — hace de C un **techo de lo que se presupone, no un suelo**:

| Historial | Relleno | Nivel | Por qué |
|---|---|---|---|
| 1 prueba de 200 | 200 | **200** | C no le inventa un nivel que no tiene: sin rampa, ya está en su sitio |
| 1 prueba de 750 | 201 | **384** | por encima de C: hay que repetirlo |
| 2 pruebas de 750 | 201 | **567** | |
| 3 pruebas de 750 | — (no entra) | **750** | con N resultados por encima de C, el relleno desaparece |
| 1 prueba de 950 | 201 | **451** | un debut excepcional no te sube a 950 |

Propiedades que el relleno **no** rompe:

- **Monotonía**: el relleno solo cede su plaza a un resultado que lo supere, así que competir sigue sin poder
  bajar el índice, ni siquiera durante la rampa.
- **No infla a nadie**: por el `min(...)`, el relleno nunca es mayor que lo que el competidor ya ha demostrado.
- **Función pura**: C es una constante del código, no depende de la población ni del momento (ver la tabla de
  alternativas descartadas en §6).

`C` vive como **constante única** (`DogRankIndex.PRIOR`): todas las disciplinas comparten la escala 0-1000, así
que un solo valor las cubre. El día que una disciplina necesite otro, pasa a ser parámetro de
`DogRankIndex.of(...)`; hasta entonces un parámetro que siempre recibe el mismo valor sería ceremonia.

**No se aplica ningún límite de "un resultado por competición"** — decisión consciente: en una competición grande (el mundial: 3 clasificatorias + final) un perro puntúa varias veces el mismo fin de semana y esas cuatro puntuaciones son cuatro pruebas juzgadas, no una. La consecuencia asumida es que las N plazas pueden salir todas del mismo fin de semana.

### 4.2 Frescura

```
frescura = frescura(antigüedad de la prueba más reciente)
```

Se evalúa sobre la prueba **más reciente, sea cual sea su puntuación** — incluso si esa prueba no entró entre las N mejores. Es lo que hace que competir en pruebas menores siempre compense: no sube el nivel, pero mantiene la frescura a 1.0.

### 4.3 Índice

```
índice = nivel × frescura
```

Redondeo final: `BigDecimal` con `RoundingMode.HALF_UP`.

Se recomienda exponer `nivel`, `frescura` e `índice` por separado, para que la UI pueda mostrar el nivel real del competidor junto a lo actual que es.

### 4.4 Ejemplos numéricos

**Campeón del mundo (950) que luego hace 3 pruebas de 750 al año**, frente a un perro regular que siempre hace 750:

| Mes | Campeón | Regular | Comentario |
|---|---|---|---|
| 8 | 817 | 750 | las tres plazas del campeón: 950 + 750 + 750 |
| 12 | 785 | 750 | competir 750 no le ha bajado nada; el 950 empieza a pesar menos |
| 16 | 750 | 750 | los 750 frescos ya contribuyen más que el 950 degradado |
| 24 | 750 | 750 | |

Con las 4 puntuaciones reales de un mundial (950/900/890/880) el campeón arranca en **913** y desciende igual de suave: 913 (todo el primer año) → 824 (mes 12) → 750 (mes 16). Nunca cae por debajo de su nivel sostenido, y nunca sube a 950 por una sola prueba.

**Perro que competía a 750 y para:** ver la tabla de §3.2 (700 a los 6 meses, 428 al año, 102 a los dos años).

**Perro con 600 sostenido durante años que un día saca un 800:** el 800 ocupa una plaza de tres → el nivel pasa de 600 a ~667, no a 800. Para ser un perro de 800 hay que sacar tres.

**Competidor esporádico** (una prueba de 750, luego 700 en el mes 15, 750 en el 17 y 800 en el 40):

| Mes | nivel | frescura | índice | |
|---|---|---|---|---|
| 0 ← 750 | 384 | 1.00 | 384 | rampa: dos plazas las ocupa el relleno |
| 12 | 359 | 0.73 | 263 | |
| 15 ← 700 | 504 | 1.00 | 504 | |
| 17 ← 750 | 671 | 1.00 | 671 | tres plazas reales: el relleno ya no interviene |
| 25 | 592 | 0.90 | 533 | |
| 39 (22 m parado) | 333 | 0.40 | 133 | |
| **40 ← 800** | 517 | 1.00 | **517** | el 800 **no** le pone en 800: sus otras dos plazas son de hace 2 años |
| 50 | 387 | 0.80 | 310 | y desde ahí **baja**, no deriva hacia 800 |
| 60 | 307 | 0.47 | 143 | |

Es el caso que motivó el cambio de modelo: con la media renormalizada anterior este perro marcaba 756 en el mes
40 y **seguía subiendo** hasta ~800 sin volver a competir. Nótese también que compite 4 veces en 40 meses, así
que un perro que hace 3 pruebas al año de 750 le supera siempre: el índice premia la densidad, no el pico.

> **Consecuencia del relleno a tener en cuenta:** como cada plaza vale al menos `min(C, mejor contribución)`, un
> competidor con un resultado reciente fuerte y el resto viejo no baja indefinidamente por el nivel, se estanca
> en `(mejor + 2C)/3` (por eso el nivel del mes 60 es 307 y no ~230). El hundimiento del inactivo lo hace la
> frescura, no el nivel — que es exactamente el reparto de responsabilidades de §4.

---

## 5. Reglas de estado

| Situación | Regla |
|---|---|
| **Provisional** | Menos de **N (3)** pruebas → el competidor **SÍ tiene índice**, pero se marca como provisional: sus plazas vacías las ocupa el relleno `C` (§4.1.1), así que su índice está por debajo de su nivel real mientras esté por encima de C. |
| **Efecto suerte** | Lo neutraliza el denominador fijo: un resultado afortunado vale como máximo `1/N` del nivel, y para sostener un índice alto hay que repetirlo N veces. |
| **Competir no penaliza** | Un resultado por debajo de tu nivel no entra entre los N mejores → no baja el índice, y sí refresca la frescura. Nunca hay incentivo para *no* competir. |
| **Inactividad / lesión** | La **frescura** degrada el índice mientras no compite. **Es recuperable**: al volver a competir vuelve a 1.0. Un lesionado baja temporalmente, no de forma permanente. |
| **Nadie desaparece** | El suelo de peso 0.01 (> 0) en ambas curvas, más un **suelo de 1 punto** en el entero final: al degradar las dos curvas a la vez, un historial de hace 5+ años redondearía a 0 y empataría a todos los retirados. Todo competidor con al menos una prueba conserva un índice ≥ 1. |
| **Viejas glorias** | Un competidor que dejó de competir se hunde hacia casi cero por la frescura **y** porque sus resultados pierden peso. En 2-3 años queda fuera de la conversación, que es la duración correcta para una carrera canina. |

> **Persistencia.** El flag **provisional** no se persiste: se calcula **al vuelo en lectura**, contando las
> filas del historial del perro en `k9x.snap_dog_rank` (`provisional = filas < 3`). No hay columna para él.

---

## 6. Decisiones descartadas (para no volver a ellas)

Todas referidas a la capa de degradación/media (la única de la que trata este spec):

| Alternativa | Por qué se descartó |
|---|---|
| Media simple de toda la carrera | Un competidor de 10 años quedaría aplastado por resultados antiguos irrelevantes. |
| Quedarse con la última prueba | Un mal día puntual hundiría el índice sin justificación. |
| **Media ponderada renormalizada de *todos* los resultados** (`Σwp/Σw`, el modelo original) | Dos defectos graves: (a) al envejecer el historial su masa de peso desaparece del denominador, así que **el índice deriva hacia la última puntuación** — con un historial fino, un solo 800 te convertía en un perro de 800; (b) **competir te podía bajar**: tres 750 recientes diluían un 950 de hace 6 meses hasta 789, castigando al campeón por ir a pruebas menores. Sustituida por mejores-N con denominador fijo (§4.1). |
| Ventana dura (solo los últimos X meses) | Provoca acantilados: el día que un resultado sale de la ventana el índice cae de golpe (en un caso real, de 750 a 517 en un mes). La curva de peso hace de ventana blanda sin ese efecto. |
| Mejores N con denominador variable (`Σw` de las seleccionadas) | Rompe la monotonía: pasar de 1 a 2 resultados podía **bajar** el índice. El denominador tiene que ser constante. |
| Shrinkage bayesiano completo (peso `k` y ancla `C` ajustables sobre todo el historial) | Sobrecomplejo. El efecto suerte lo cubre el denominador fijo; lo único que se conserva de esa familia es el **relleno acotado** de las plazas vacías (§4.1.1), que es un solo número y solo afecta a los provisionales. |
| Contar las plazas vacías como **0** | Deja a un debutante con un 750 en un nivel de 250: técnicamente coherente pero no comunicable en la UI. Sustituido por el relleno `C`. |
| Relleno `C` **sin** el `min(...)` (C plano) | Infla a los competidores flojos: un perro cuyo techo real es 200 marcaría 450 con una sola prueba. C tiene que ser un techo de lo presupuesto, no un suelo. |
| `C` = mediana real de la población, recalculada por el cron | Rompe que el índice sea función pura del historial del competidor: tu número cambiaría porque *otros* han competido, y dejaría de ser reproducible al recalcular. Además hoy no hay volumen para una mediana estable. |
| Penalización explícita/sustractiva por inactividad (restar N puntos) | Doble castigo y no recuperable. En su lugar se usa la **frescura multiplicativa** (§4.2). |
| Bajar el techo de la frescura (p. ej. 0.98 en vez de 1.0) para suavizar el salto al volver de un parón | No sirve: el salto vale `nivel − nivel·frescura(parón)`, o sea depende de **lo hondo que hayas caído**, no del techo. Bajar el techo a 0.98 reducía un salto de +207 a +203. |

**Aparcado, no descartado — suavizado temporal.** Un filtro `índice_t = α·crudo_t + (1−α)·índice_{t−1}` con `α = Δmeses/(Δmeses + τ)` convierte cualquier escalón en una rampa de varios meses (con τ=3, un salto de +250 se reparte en ~+40/mes). Se deja fuera de esta versión porque introduce **estado**: el índice dejaría de ser función pura del historial y cualquier corrección o backfill de un resultado antiguo exigiría *replay* de toda la serie posterior. Si los escalones de la rampa de entrada o de la vuelta tras un parón resultan molestos en producción, este es el siguiente paso y encaja encima de todo lo demás sin cambiarlo.

---

## 7. Parámetros (resumen para calibrar)

| Parámetro | Valor | Nota |
|---|---|---|
| **N** (plazas del nivel y denominador) | **3** | Con 2 se llega antes al nivel real pero un resultado vale la mitad del índice |
| **C** (relleno de plazas vacías) | **201** = suelo de la franja `FCI_GRADE_1` | Por disciplina; acotado por `min(C, mejor contribución)`. Solo afecta a los provisionales |
| Meseta NIVEL | 8 meses | El grueso de una temporada cuenta pleno |
| Anclajes NIVEL | 8→1.0, 14→0.85, 20→0.65, 26→0.45, 32→0.25, 44→0.05, 56→0.01 | Horizonte ≈ mitad de una carrera canina |
| Meseta FRESCURA | 6 meses | Hueco normal entre pruebas de una temporada |
| Anclajes FRESCURA | 6→1.0, 10→0.80, 16→0.60, 22→0.40, 28→0.25, 34→0.12, 46→0.03, 58→0.01 | Un año parado ≈ −43 %; dos años ≈ −86 % |
| Suelo de peso | 0.01 permanente (mes 56 nivel / 58 frescura) | > 0, garantiza que nadie desaparece |
| Umbral provisional | N (3) pruebas | <3 → marcado, pero con índice |
| Límite por competición | ninguno | Decisión consciente (§4.1) |

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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class CompetitorIndexCalculator {

    private static final int N = 3;                     // level slots AND the fixed denominator
    private static final double WEIGHT_FLOOR = 0.01;
    // C, the empty-slot filler, is a parameter: it is per discipline (OBDX: 201).

    // LEVEL curve: weighs results against each other. 8-month plateau (most of a season).
    private static final double LEVEL_PLATEAU_MONTHS = 8.0;
    private static final double[][] LEVEL_ANCHORS = {
        {8, 1.00}, {14, 0.85}, {20, 0.65}, {26, 0.45}, {32, 0.25}, {44, 0.05}, {56, 0.01}
    };

    // FRESHNESS curve: shorter plateau (6, the normal gap between trials) and a steeper drop — a one-year
    // layoff must bite (≈ −43%).
    private static final double FRESHNESS_PLATEAU_MONTHS = 6.0;
    private static final double[][] FRESHNESS_ANCHORS = {
        {6, 1.00}, {10, 0.80}, {16, 0.60}, {22, 0.40},
        {28, 0.25}, {34, 0.12}, {46, 0.03}, {58, 0.01}
    };

    /** The competitor's score for one event: a black box, already computed by the scoring layer. */
    public record Result(LocalDate date, BigDecimal competitorEventScore) {}
    public record Index(BigDecimal value, BigDecimal level, BigDecimal freshness,
                        boolean provisional, int resultCount) {}

    /**
     * @param filler the discipline's prior C (OBDX: the floor of the FCI_GRADE_1 band, 201). Passed in so the
     *               index calculation stays agnostic of any particular discipline.
     */
    public Index calculate(List<Result> results, LocalDate today, BigDecimal filler) {
        if (results.isEmpty()) {
            return new Index(null, null, null, true, 0);
        }

        // Each result's contribution: age weight × score, best first.
        List<BigDecimal> contributions = results.stream()
                .map(r -> {
                    double months = months(r.date(), today);
                    return BigDecimal.valueOf(interpolate(months, LEVEL_PLATEAU_MONTHS, LEVEL_ANCHORS))
                            .multiply(r.competitorEventScore());
                })
                .sorted(Comparator.reverseOrder())
                .toList();

        // Empty slots are worth min(C, own best contribution): C is a ceiling on what is assumed, never a
        // floor — a competitor whose only result is a 200 must not be lifted to C.
        BigDecimal emptySlot = filler.min(contributions.get(0));

        // Level: the N best among real contributions and empty slots, over the FIXED denominator N. A worse
        // result never displaces a better one, so competing can never lower the index.
        BigDecimal level = Stream.concat(contributions.stream(),
                        Stream.generate(() -> emptySlot).limit(N))
                .sorted(Comparator.reverseOrder())
                .limit(N)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(N), 4, RoundingMode.HALF_UP);

        // Freshness: its own curve on the most recent result, whatever that result scored — competing in minor
        // trials keeps freshness at 1.0 even when it adds nothing to the level.
        double mostRecentMonths = results.stream()
                .mapToDouble(r -> months(r.date(), today)).min().orElseThrow();
        BigDecimal freshness = BigDecimal.valueOf(
                interpolate(mostRecentMonths, FRESHNESS_PLATEAU_MONTHS, FRESHNESS_ANCHORS));

        // Floor of 1: with both curves degrading at once, a 5-year-old history would round down to 0 and tie
        // every retired competitor together.
        BigDecimal index = level.multiply(freshness).max(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);

        return new Index(index, level.setScale(2, RoundingMode.HALF_UP),
                         freshness, results.size() < N, results.size());
    }

    private double months(LocalDate date, LocalDate today) {
        return Math.max(0, ChronoUnit.DAYS.between(date, today)) / 30.4375;
    }

    /** Linear interpolation over an anchor curve: plateau, ramp, floor. */
    private double interpolate(double months, double plateau, double[][] anchors) {
        if (months <= plateau) return 1.0;
        double[] last = anchors[anchors.length - 1];
        if (months >= last[0]) return WEIGHT_FLOOR;
        for (int i = 0; i < anchors.length - 1; i++) {
            double m0 = anchors[i][0],  w0 = anchors[i][1];
            double m1 = anchors[i + 1][0], w1 = anchors[i + 1][1];
            if (months >= m0 && months <= m1) {
                double t = (months - m0) / (m1 - m0);
                return w0 + t * (w1 - w0);
            }
        }
        return WEIGHT_FLOOR;
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

> El índice es **función pura** de (historial de `snap_dog_rank`, instante de evaluación): recalcularlo desde
> cero siempre da el mismo número. Esa propiedad es la que permite borrar y reconstruir
> `snap_dog_index_history` sin miedo, y la que se perdería si algún día se añade el suavizado temporal (§6).

---

## 11. Ingesta de eventos históricos (backfill)

Para añadir una prueba pasada (p. ej. eventos previos al Trofeu) **no se toca ninguna tabla snap**; basta con
crear el evento como si fuera real:

1. `k9x.competitions` + `k9x.stages` con las **fechas reales** de la prueba (`date_to` será el
   `applying_timestamp` de todo lo derivado).
2. `k9x.events`: `discipline`, un `configuration_id` **existente en código** (los `configuration.json` de
   `.../disciplines/obdx/federations/` y las franjas de `ObdxConfigurationsRankThresholds` — no hay tablas de
   configuración) y **⚠️ `rank_score` puesto a mano**: esa fórmula corre al guardar el evento por la API, no
   en el cron. Sin `rank_score`, los competidores no puntúan y el evento no afecta al índice.
3. `obdx.event_info` con la **`category`** de la prueba (obligatoria: es lo que fija la sub-banda de la que
   sale el `rank_score` del punto anterior).
4. `obdx.event_judges`, `obdx.event_exercises` (con los jueces asignados por ejercicio: la clasificación solo
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

## 12. Migración desde el modelo anterior

El modelo anterior (media ponderada renormalizada de todos los resultados, meseta 10 meses, suelo en el mes 70)
está implementado en `DogRankIndex` (dominio) y consumido por `GenerateDogRankHistoryServiceCase` (cron
quincenal). Al adoptar este spec cambia:

1. **`DogRankIndex`**: selección de las N mejores contribuciones, relleno `min(C, mejor contribución)` de las
   plazas vacías y denominador fijo N; anclajes nuevos de las dos curvas; **meseta por curva** (8 nivel /
   6 frescura — hoy hay una sola constante compartida de 10) y suelo en el mes **56/58** en vez de 70. `C` es la
   constante `DogRankIndex.PRIOR = ObdxConfigurationsRankThresholds.FCI_GRADE_1.min()`. Se añade además el suelo
   de 1 punto en el entero final (§5).
2. **`GenerateDogRankHistoryServiceCase`**: usa el mes de suelo para saber hasta cuándo emitir registros
   `TIME_DEGRADATION`; pasa de 70 al suelo de la **curva de frescura** (58), que es la que sigue moviendo el
   índice de un inactivo. El resto del replay no cambia.
3. **`dogs.rank` y `k9x.snap_dog_index_history`**: los valores existentes quedan obsoletos. Como el índice es
   función pura del historial (§10), la migración es **borrar `snap_dog_index_history`** y dejar que el cron
   quincenal reconstruya la línea temporal; `snap_dog_rank` **no** se toca (es el insumo crudo, sigue siendo
   válido).
4. **Tests de dominio**: los casos de la meseta, del suelo y de la media ponderada cambian de valor esperado;
   hay que añadir los tres invariantes de §1 (competir no baja el índice, un pico vale `1/N`, la rampa de
   entrada) como tests explícitos.

---

## Resumen en una frase

> El índice es **nivel × frescura** sobre las `puntuacionEventoCompetidor` (caja negra) de un competidor: el *nivel* es la suma de sus **3 mejores** resultados ponderados por antigüedad (meseta de 8 meses, residual a los 44) dividida siempre entre **3**, de modo que competir nunca te baja y un solo resultado excepcional vale un tercio; las plazas que aún no ha llenado valen `min(C, su mejor resultado)` con **C = 201** (suelo de la franja FCI grado 1), lo que le da el beneficio de la duda hasta ese nivel sin inflar a nadie; la *frescura* es una curva más agresiva evaluada en su prueba más reciente (medio año parado ≈ −7 %, un año ≈ −43 %, dos años ≈ −86 %), calibrada a una carrera canina de 5-8 años y recuperable en cuanto vuelve a competir. Con menos de 3 pruebas se marca provisional, y nadie desaparece nunca.
