package com.k9x.application.stages.use_case.dto;

import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.domain.competitions.aggregates.CompetitionExtraction;

import java.util.List;

public record FetchStageDetailDTO(String id, String name, String competitionName, Long dateFrom, Long dateTo,
                                  String address, String organizer, String status, Long deletedAt,
                                  List<FetchStageDetailEventDTO> events,
                                  List<StageNotificationDTO> notifications, CompetitionExtraction extraction) {
}
