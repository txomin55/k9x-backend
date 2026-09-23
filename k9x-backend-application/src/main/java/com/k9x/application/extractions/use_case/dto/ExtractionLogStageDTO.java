package com.k9x.application.extractions.use_case.dto;

import java.util.List;

public record ExtractionLogStageDTO(String id, String name, String competitionName, String country,
                                    Long dateFrom, Long dateTo, List<ExtractionLogEventDTO> events) {
}
