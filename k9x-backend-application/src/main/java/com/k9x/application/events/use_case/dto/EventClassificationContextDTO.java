package com.k9x.application.events.use_case.dto;

import com.k9x.domain.events.aggregates.EventSnapshot;

/**
 * What a classification is built from, kept for a short while so a live classification polled every few seconds
 * does not reread it: the header, and the event with its scores once it had to be loaded — {@code null} while
 * the stored snapshot was enough.
 */
public record EventClassificationContextDTO(FetchEventClassificationHeaderDTO header, EventSnapshot event) {

    public EventClassificationContextDTO withEvent(EventSnapshot loaded) {
        return new EventClassificationContextDTO(header, loaded);
    }
}
