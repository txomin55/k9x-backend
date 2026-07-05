package com.k9x.application.events.obdx.use_case.dto;

import java.util.List;

public record FetchObdxClassificationDTO(Long scoresLastUpdate,
                                         List<FetchClassificationCompetitorDTO> competitors,
                                         String scoreCalculation,
                                         List<FetchObdxEventJudgeDTO> judges) {
}
