package com.k9x.application.stages.use_case.dto;

import java.util.List;

public record FetchStageDetailDTO(String id, String name, String competitionName, Long dateFrom, Long dateTo,
                                  String address, String organizer, String status, Long deletedAt,
                                  List<FetchStageDetailEventDTO> events) {
}
