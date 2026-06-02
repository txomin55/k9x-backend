package com.k9x.application.events.obdx.use_cases.dto;

import java.math.BigDecimal;

public record FetchClassificationRawRowDTO(
        String dogId, String dogName, String dogOwner, String dogTeam, String dogCountry,
        String exerciseId, short exercisePosition, String[] exerciseTags,
        String judgeId, String judgeName,
        BigDecimal score, Long scoreLastUpdate) {
}
