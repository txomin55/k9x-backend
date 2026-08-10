package com.k9x.application.competitions.use_case.dto;

import com.k9x.application.shared.IdNameDTO;

import java.util.List;

public record FetchSelectableStageDTO(String id, String name, List<IdNameDTO> events) {
}
