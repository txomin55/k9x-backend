package com.k9x.application.collections.use_case.dto;

import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;

import java.math.BigDecimal;
import java.util.List;

public record FetchCollectionDetailDTO(
    String competitionName,
    String eventName,
    String configurationId,
    String discipline,
    List<BigDecimal> allowedValues,
    FetchObdxCollectionDTO obdx
) {}
