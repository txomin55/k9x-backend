package com.k9x.application.events.obdx.use_cases.dto;

import java.util.List;

public record FetchObdxClassificationDTO(Long scoresLastUpdate,
                                         List<FetchClassificationCompetitorDTO> competitors) {
}
