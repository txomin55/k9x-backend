package com.k9x.application.competitions.use_case.dto;

import java.util.List;

/**
 * Identifiers and names only, for pickers. Deliberately not the full {@code FetchCompetitionDTO}: that one
 * hydrates the whole aggregate, which is far more than a select needs.
 */
public record FetchSelectableCompetitionDTO(String id, String name, List<FetchSelectableStageDTO> stages) {
}
