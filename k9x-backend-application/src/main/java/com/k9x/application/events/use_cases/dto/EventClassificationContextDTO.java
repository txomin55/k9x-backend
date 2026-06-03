package com.k9x.application.events.use_cases.dto;

import com.k9x.domain.aggregates.events.Event;

public record EventClassificationContextDTO(Event event, String stageName) {
}
