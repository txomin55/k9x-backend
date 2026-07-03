package com.k9x.application.events.obdx.use_case.dto;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

public record FetchObdxEventDTO(String id, String name, String stageId, String stageName, String discipline,
                                String status, Long enrollmentDeadline, ObdxAvgMethod scoreCalculation) {
}
