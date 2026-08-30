package com.k9x.application.events.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.domain.events.aggregates.EventSnapshot;

public record EventClassificationContextDTO(EventSnapshot event, String stageName, long stageDateTo,
                                            String competitionName, CompetitionExtraction competitionExtraction) {
}
