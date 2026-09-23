package com.k9x.application.extractions.use_case.dto;

public record ExtractionLogEventDTO(String id, String name, String disciplineId, int competitorCount,
                                    String rank) {
}
