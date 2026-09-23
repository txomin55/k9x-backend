package com.k9x.application.methodology.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.methodology.port.GetMethodologyCatalogPort;
import com.k9x.application.methodology.use_case.dto.K9xMethodologyDTO;
import com.k9x.application.methodology.use_case.dto.LocalizedTextDTO;
import com.k9x.application.methodology.use_case.dto.MethodologyCatalogDTO;
import com.k9x.domain.disciplines.obdx.ObdxCompetitorEventScore;
import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.disciplines.obdx.ObdxScoreRating;
import com.k9x.domain.dogs.rank.DogRankIndex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The K9X index methodology page as data: the two decay curves of {@link DogRankIndex} and a worked example whose
 * every figure is computed with the real formulas — the slot filling with {@link DogRankIndex#of}, each example
 * result with {@link ObdxCompetitorEventScore}, and each example index line with {@link DogRankIndex#exactOf}.
 * Only the example's inputs are a fixture ({@link K9xIndexExampleFixture}). Public: it asserts nothing about the
 * caller.
 */
public class GetK9xMethodologyServiceCase {

    /** Contract version of {@code GET /k9x/methodology}; bumped by any change that breaks a consumer. */
    public static final int SCHEMA_VERSION = 1;

    private static final String FORMULA = "index = level × freshness";
    private static final String FILLER = "min(C, bestContribution)";

    /** The band whose floor is the prior C that fills empty slots ({@link DogRankIndex#PRIOR}). */
    private static final ObdxConfigurationsRankThresholds PRIOR_BAND = ObdxConfigurationsRankThresholds.FCI_GRADE_1;

    /** Example index lines are sampled every other month, besides the instants where they change slope. */
    private static final int SAMPLE_EVERY_MONTHS = 2;

    private final GetMethodologyCatalogPort getMethodologyCatalogPort;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;

    public GetK9xMethodologyServiceCase(GetMethodologyCatalogPort getMethodologyCatalogPort,
                                        GetObdxClassificationConfigPort getObdxClassificationConfigPort) {
        this.getMethodologyCatalogPort = getMethodologyCatalogPort;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
    }

    public K9xMethodologyDTO getMethodology() {
        MethodologyCatalogDTO catalog = getMethodologyCatalogPort.getCatalog();
        return new K9xMethodologyDTO(SCHEMA_VERSION,
                new K9xMethodologyDTO.ScoreRange(ObdxRank.SCALE_MIN, ObdxRank.SCALE_MAX),
                decayCurves(),
                new K9xMethodologyDTO.IndexExample(FORMULA, parameters(catalog), slotFilling(),
                        K9xIndexExampleFixture.DOGS.stream().map(dog -> exampleDog(dog, catalog)).toList()));
    }

    private static List<K9xMethodologyDTO.DecaySeries> decayCurves() {
        return List.of(
                decaySeries("level", DogRankIndex.LEVEL_PLATEAU_MONTHS_THRESHOLD, DogRankIndex.levelAnchors()),
                decaySeries("freshness", DogRankIndex.FRESHNESS_PLATEAU_MONTHS_THRESHOLD, DogRankIndex.freshnessAnchors()));
    }

    private static K9xMethodologyDTO.DecaySeries decaySeries(String id, int plateauMonths,
                                                             List<DogRankIndex.Anchor> anchors) {
        return new K9xMethodologyDTO.DecaySeries(id, plateauMonths, anchors.getLast().month(),
                BigDecimal.valueOf(DogRankIndex.FLOOR),
                anchors.stream()
                        .map(anchor -> new K9xMethodologyDTO.Anchor(anchor.month(), BigDecimal.valueOf(anchor.weight())))
                        .toList());
    }

    private static K9xMethodologyDTO.IndexParameters parameters(MethodologyCatalogDTO catalog) {
        LocalizedTextDTO bandName = catalog.configurationOf(PRIOR_BAND).name();
        return new K9xMethodologyDTO.IndexParameters(DogRankIndex.SLOTS, DogRankIndex.PRIOR.intValueExact(),
                new LocalizedTextDTO("Suelo de la franja " + bandName.es(), bandName.en() + " band floor"),
                FILLER, DogRankIndex.SLOTS);
    }

    /** The ramp of 1..N results of the same score, then a single weak result showing the prior is a ceiling. */
    private static List<K9xMethodologyDTO.SlotFillingCase> slotFilling() {
        List<K9xMethodologyDTO.SlotFillingCase> cases = new ArrayList<>();
        for (int count = 1; count <= DogRankIndex.SLOTS; count++) {
            cases.add(slotFillingCase("ramp-" + count, Collections.nCopies(count, K9xIndexExampleFixture.RAMP_SCORE)));
        }
        cases.add(slotFillingCase("weak-result", List.of(K9xIndexExampleFixture.WEAK_SCORE)));
        return cases;
    }

    private static K9xMethodologyDTO.SlotFillingCase slotFillingCase(String id, List<Integer> results) {
        List<Integer> sorted = results.stream().sorted(Comparator.reverseOrder()).toList();
        int filler = Math.min(DogRankIndex.PRIOR.intValueExact(), sorted.getFirst());
        List<Integer> slots = Stream.concat(sorted.stream(), Stream.generate(() -> filler))
                .limit(DogRankIndex.SLOTS)
                .toList();
        int level = DogRankIndex.of(results.stream()
                .map(score -> new DogRankIndex.Result(BigDecimal.valueOf(score), 0L))
                .toList(), 0L);
        return new K9xMethodologyDTO.SlotFillingCase(id, results, slots, level);
    }

    private K9xMethodologyDTO.ExampleDog exampleDog(K9xIndexExampleFixture.Dog dog, MethodologyCatalogDTO catalog) {
        List<Scored> scored = new ArrayList<>();
        for (K9xIndexExampleFixture.Event event : dog.events()) {
            event.results().forEach(result -> scored.add(new Scored(event.month(), result, score(result, catalog))));
        }

        TreeSet<Integer> eventMonths = dog.events().stream()
                .map(K9xIndexExampleFixture.Event::month)
                .collect(Collectors.toCollection(TreeSet::new));
        TreeSet<Integer> months = IntStream.rangeClosed(0, K9xIndexExampleFixture.HORIZON_MONTHS / SAMPLE_EVERY_MONTHS)
                .mapToObj(step -> step * SAMPLE_EVERY_MONTHS)
                .collect(Collectors.toCollection(TreeSet::new));
        for (int month : eventMonths) {
            months.add(month);
            for (int plateau : List.of(DogRankIndex.FRESHNESS_PLATEAU_MONTHS_THRESHOLD, DogRankIndex.LEVEL_PLATEAU_MONTHS_THRESHOLD)) {
                if (month + plateau <= K9xIndexExampleFixture.HORIZON_MONTHS) {
                    months.add(month + plateau);
                }
            }
        }

        List<K9xMethodologyDTO.ExampleIndexPoint> series = new ArrayList<>();
        for (int month : months) {
            if (eventMonths.contains(month) && scored.stream().anyMatch(result -> result.month() < month)) {
                series.add(new K9xMethodologyDTO.ExampleIndexPoint(month, indexAt(scored, month, false)));
            }
            series.add(new K9xMethodologyDTO.ExampleIndexPoint(month, indexAt(scored, month, true)));
        }

        List<K9xMethodologyDTO.ExampleEvent> events = dog.events().stream()
                .map(event -> exampleEvent(event, scored, catalog))
                .toList();
        List<K9xMethodologyDTO.ExampleResult> results = scored.stream()
                .map(result -> new K9xMethodologyDTO.ExampleResult(result.month(), result.score()))
                .toList();
        return new K9xMethodologyDTO.ExampleDog(dog.id(), dog.name(), results, series, events);
    }

    private K9xMethodologyDTO.ExampleEvent exampleEvent(K9xIndexExampleFixture.Event event, List<Scored> scored,
                                                        MethodologyCatalogDTO catalog) {
        List<Scored> own = scored.stream()
                .filter(result -> result.month() == event.month() && !isPrior(result.result()))
                .toList();
        BigDecimal score = own.stream().map(Scored::score).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(own.size()), 1, RoundingMode.HALF_UP);
        return new K9xMethodologyDTO.ExampleEvent(event.month(), indexAt(scored, event.month(), true), score,
                label(event, own, catalog));
    }

    /** A short technical label: {@code G3 250/320 · ev.750}, or {@code 3 × G3 · scores 750/700/650}. */
    private String label(K9xIndexExampleFixture.Event event, List<Scored> own, MethodologyCatalogDTO catalog) {
        String label;
        if (own.size() == 1 && own.getFirst().result() instanceof K9xIndexExampleFixture.Merit merit) {
            label = K9xIndexExampleFixture.shortName(merit.band()) + " " + merit.total().toPlainString() + "/"
                    + maxScore(config(merit.band(), catalog)).toPlainString() + " · ev." + merit.eventScore();
        } else {
            label = own.size() + " × " + K9xIndexExampleFixture.shortName(band(own.getFirst().result()))
                    + " · scores " + own.stream()
                    .map(result -> result.score().stripTrailingZeros().toPlainString())
                    .collect(Collectors.joining("/"));
        }
        List<K9xIndexExampleFixture.Result> priors = event.results().stream().filter(GetK9xMethodologyServiceCase::isPrior).toList();
        if (!priors.isEmpty()) {
            label += " (+" + priors.size() + " earlier " + K9xIndexExampleFixture.shortName(band(priors.getFirst())) + ")";
        }
        return label;
    }

    private BigDecimal score(K9xIndexExampleFixture.Result result, MethodologyCatalogDTO catalog) {
        return switch (result) {
            case K9xIndexExampleFixture.Direct direct -> direct.score();
            case K9xIndexExampleFixture.Merit merit -> {
                ObdxClassificationConfigDTO config = config(merit.band(), catalog);
                List<BigDecimal> thresholds = config.qualifications().stream()
                        .map(ObdxClassificationConfigDTO.QualificationThreshold::minScore)
                        .sorted()
                        .toList();
                yield ObdxCompetitorEventScore.of(merit.eventScore(), merit.band().min(), thresholds.getFirst(),
                        thresholds.getLast(), merit.total(), maxScore(config));
            }
        };
    }

    private ObdxClassificationConfigDTO config(ObdxConfigurationsRankThresholds band, MethodologyCatalogDTO catalog) {
        return getObdxClassificationConfigPort.getConfig(catalog.configurationOf(band).configurationId());
    }

    private static BigDecimal maxScore(ObdxClassificationConfigDTO config) {
        return ObdxScoreRating.maxPossibleTotal(config.maxAllowedScore(), config.coefByExerciseId(),
                config.coefByExerciseId().keySet());
    }

    /** The exact index at {@code month}, with or without the results earned that very month. */
    private static BigDecimal indexAt(List<Scored> scored, int month, boolean inclusive) {
        long now = DogRankIndex.plusMonths(0L, month);
        List<DogRankIndex.Result> history = scored.stream()
                .filter(result -> inclusive ? result.month() <= month : result.month() < month)
                .map(result -> new DogRankIndex.Result(result.score(), DogRankIndex.plusMonths(0L, result.month())))
                .toList();
        return DogRankIndex.exactOf(history, now).setScale(1, RoundingMode.HALF_UP);
    }

    private static boolean isPrior(K9xIndexExampleFixture.Result result) {
        return result instanceof K9xIndexExampleFixture.Direct direct && direct.prior();
    }

    private static ObdxConfigurationsRankThresholds band(K9xIndexExampleFixture.Result result) {
        return switch (result) {
            case K9xIndexExampleFixture.Direct direct -> direct.band();
            case K9xIndexExampleFixture.Merit merit -> merit.band();
        };
    }

    /** A fixture result with the month it was earned and the score the domain gives it. */
    private record Scored(int month, K9xIndexExampleFixture.Result result, BigDecimal score) {
    }
}
