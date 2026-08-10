package com.k9x.application.rankings.use_case.dto;

import java.util.List;

/**
 * Public read model of a ranking's results.
 *
 * <p>{@code groupBy} is carried for the REST boundary, which needs it to translate country criteria; it is
 * not part of the public response.
 */
public record FetchRankingClassificationDTO(
        List<FetchRankingClassificationEventDTO> events,
        List<FetchRankingClassificationGroupDTO> groups,
        String groupBy
) {
}
