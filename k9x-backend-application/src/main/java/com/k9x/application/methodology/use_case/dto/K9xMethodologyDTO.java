package com.k9x.application.methodology.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

/** What {@code GET /k9x/methodology} returns: the K9X index curves and a worked example of them. */
public record K9xMethodologyDTO(int schemaVersion, ScoreRange indexScale, List<DecaySeries> decayCurves,
                                IndexExample indexExample) {

    public record ScoreRange(int min, int max) {
    }

    public record Anchor(int month, BigDecimal weight) {
    }

    public record DecaySeries(String id, int plateauMonths, int floorFromMonth, BigDecimal floorValue,
                              List<Anchor> anchors) {
    }

    public record IndexParameters(int slots, int prior, LocalizedTextDTO priorReference, String filler,
                                  int provisionalIfResultsBelow) {
    }

    public record SlotFillingCase(String id, List<Integer> results, List<Integer> slots, int level) {
    }

    public record ExampleResult(int month, BigDecimal score) {
    }

    public record ExampleIndexPoint(int month, BigDecimal index) {
    }

    public record ExampleEvent(int month, BigDecimal index, BigDecimal score, String label) {
    }

    public record ExampleDog(String id, LocalizedTextDTO name, List<ExampleResult> results,
                             List<ExampleIndexPoint> series, List<ExampleEvent> events) {
    }

    public record IndexExample(String formula, IndexParameters parameters, List<SlotFillingCase> slotFilling,
                               List<ExampleDog> dogs) {
    }
}
