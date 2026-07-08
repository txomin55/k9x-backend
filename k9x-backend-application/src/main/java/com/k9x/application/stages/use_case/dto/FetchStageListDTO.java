package com.k9x.application.stages.use_case.dto;

import java.util.List;

public record FetchStageListDTO(String id, String name, String competitionName, String country,
                                String address, Double coordAlt, Double coordLong,
                                Long dateFrom, Long dateTo,
                                String organizer, List<FetchStageListEventDTO> events, String status) {
}
