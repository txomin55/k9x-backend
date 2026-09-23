package com.k9x.application.extractions.use_case.dto;

import java.util.List;

/** The stages whose extraction was loaded on one UTC day; {@code date} is the start of that day. */
public record ExtractionLogDayDTO(long date, List<ExtractionLogStageDTO> stages) {
}
