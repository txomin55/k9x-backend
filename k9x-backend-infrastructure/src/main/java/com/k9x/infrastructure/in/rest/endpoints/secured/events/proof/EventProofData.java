package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import java.util.List;

/**
 * Everything one booklet strip prints, already resolved by the caller: the qualification arrives translated
 * and the class arrives as text, because the strip is a fixed paper form and the writer must not have an
 * opinion about domain vocabulary.
 *
 * <p>{@code dateFrom} is the stage start in epoch millis and is rendered as a UTC day, the same way
 * {@link EventWorkbookWriter} renders the enrollment deadline: the strip is glued into a physical booklet, so
 * the printed day must not shift with the reader's timezone.
 *
 * <p>Any field may be null or blank; a blank value simply prints an empty bordered cell to fill in by hand.
 */
public record EventProofData(
        String eventName,
        Long dateFrom,
        String organizerName,
        String address,
        String className,
        String totalScore,
        String qualification,
        String commissioner,
        Integer position,
        String handler,
        String dogName,
        List<Judge> judges) {

    public EventProofData {
        judges = judges == null ? List.of() : List.copyOf(judges);
    }

    /** Ordered as printed. {@code mainJudge} marks the one cell labelled "Juez PRINCIPAL - ÁRBITRO". */
    public record Judge(String name, boolean mainJudge) {
    }
}
