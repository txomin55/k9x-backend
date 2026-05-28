package com.k9x.application.stages.use_case.dto;

import java.util.List;

public record FetchStageDetailDTO(String id, String name, Long dateFrom, Long dateTo,
                                  String address, String organizer, Long deletedAt,
                                  List<FetchStageDetailEventDTO> events) {
}
