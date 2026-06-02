package com.k9x.application.competitions.use_case.dto;

import java.util.List;

public record FetchStageDTO(String id, String name, Long dateFrom, Long dateTo, List<FetchEventDTO> events) {
}
