package com.k9x.application.methodology.use_case;

import com.k9x.application.methodology.use_case.dto.LocalizedTextDTO;
import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds.CPC_COBS;
import static com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds.FCI_GRADE_1;
import static com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds.FCI_GRADE_2;
import static com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds.FCI_GRADE_3;

/**
 * The four career profiles the K9X methodology page tells the index with. It is narrative, so it lives here as a
 * fixture — but only its <em>inputs</em> do: when each dog competed and what it got. Every score, index and label
 * the page shows is computed from them with the real merit curve and index formula
 * ({@link GetK9xMethodologyServiceCase}).
 */
final class K9xIndexExampleFixture {

    /** Months each profile is followed for. */
    static final int HORIZON_MONTHS = 60;

    /** The score repeated along the slot-filling ramp, and the weak one that shows the prior is a ceiling. */
    static final int RAMP_SCORE = 750;
    static final int WEAK_SCORE = 200;

    /** One result: its score given outright, or earned in an OBDX event through the merit curve. */
    sealed interface Result permits Direct, Merit {
    }

    /**
     * A score given outright. {@code prior} marks a result from before the story starts: it counts for the index
     * and is drawn, but the event it is part of is not about it.
     */
    record Direct(ObdxConfigurationsRankThresholds band, BigDecimal score, boolean prior) implements Result {
    }

    /** A result earned in an event of {@code eventScore} with {@code total} points. */
    record Merit(ObdxConfigurationsRankThresholds band, int eventScore, BigDecimal total) implements Result {
    }

    record Event(int month, List<Result> results) {
    }

    record Dog(String id, LocalizedTextDTO name, List<Event> events) {
    }

    static final List<Dog> DOGS = List.of(
            new Dog("dogA", new LocalizedTextDTO("Perro A (se jubila)", "Dog A (retires)"), List.of(
                    event(0, direct(FCI_GRADE_3, "750"), direct(FCI_GRADE_3, "700"), direct(FCI_GRADE_3, "650")))),
            new Dog("dogB", new LocalizedTextDTO("Perro B (progresión)", "Dog B (progression)"), List.of(
                    event(0, merit(CPC_COBS, 150, "180")),
                    event(10, merit(FCI_GRADE_1, 230, "260")),
                    event(20, merit(FCI_GRADE_2, 540, "230")),
                    event(30, merit(FCI_GRADE_2, 550, "270")),
                    event(37, merit(FCI_GRADE_3, 750, "250")),
                    event(40, merit(FCI_GRADE_3, 760, "255")))),
            new Dog("dogC", new LocalizedTextDTO("Perro C (estable top)", "Dog C (steady top)"), steady(
                    List.of(prior("678.9"), prior("660")),
                    new int[][]{{780, 270}, {750, 265}, {800, 280}, {720, 255}, {770, 270}, {780, 270}, {750, 265},
                            {800, 280}, {720, 255}})),
            new Dog("dogD", new LocalizedTextDTO("Perro D (estable estándar)", "Dog D (steady standard)"), steady(
                    List.of(prior("620"), prior("606")),
                    new int[][]{{650, 240}, {680, 250}, {620, 230}, {700, 260}, {660, 245}, {650, 240}, {680, 250},
                            {620, 230}, {700, 260}})));

    /** Short name of a configuration in the event labels. */
    static String shortName(ObdxConfigurationsRankThresholds band) {
        return switch (band) {
            case CPC_COBS -> "COBS";
            case FCI_GRADE_1 -> "G1";
            case FCI_GRADE_2 -> "G2";
            case FCI_GRADE_3 -> "G3";
            default -> band.configurationKey();
        };
    }

    /** A grade 3 regular: two earlier results, then one event every five months. */
    private static List<Event> steady(List<Result> priors, int[][] eventScoreAndTotal) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < eventScoreAndTotal.length; i++) {
            Result result = merit(FCI_GRADE_3, eventScoreAndTotal[i][0], String.valueOf(eventScoreAndTotal[i][1]));
            List<Result> results = i == 0
                    ? Stream.concat(priors.stream(), Stream.of(result)).toList()
                    : List.of(result);
            events.add(new Event(i * 5, results));
        }
        return List.copyOf(events);
    }

    private static Event event(int month, Result... results) {
        return new Event(month, List.of(results));
    }

    private static Result direct(ObdxConfigurationsRankThresholds band, String score) {
        return new Direct(band, new BigDecimal(score), false);
    }

    private static Result prior(String score) {
        return new Direct(FCI_GRADE_3, new BigDecimal(score), true);
    }

    private static Result merit(ObdxConfigurationsRankThresholds band, int eventScore, String total) {
        return new Merit(band, eventScore, new BigDecimal(total));
    }

    private K9xIndexExampleFixture() {
    }
}
