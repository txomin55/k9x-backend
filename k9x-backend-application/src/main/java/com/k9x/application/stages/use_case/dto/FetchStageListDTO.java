package com.k9x.application.stages.use_case.dto;

import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSource;

import java.util.List;

/**
 * The stage read models carry the competition's {@code source} the same way they carry its address: a stage has
 * no origin of its own, it inherits the one of the competition it belongs to.
 */
public record FetchStageListDTO(String id, String name, String competitionName, String country,
                                String address, Double coordAlt, Double coordLong,
                                Long dateFrom, Long dateTo,
                                String organizer, List<FetchStageListEventDTO> events, String status,
                                List<StageNotificationDTO> notifications,
                                boolean includesRankings, CompetitionSource source) {
}
