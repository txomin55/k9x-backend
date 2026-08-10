package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingDetailPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;
import com.k9x.application.utils.auth.AuthAssertions;

import java.util.Optional;

/**
 * Read-only, so it deliberately does not implement {@code TransactionalUseCase}.
 *
 * <p>The lookup is scoped by creator inside the query: another organizer's ranking is reported as
 * missing rather than forbidden, so the endpoint never reveals that the identifier is taken.
 */
public class GetRankingServiceCase {

    private final GetRankingDetailPersistencePort getRankingDetailPersistencePort;

    public GetRankingServiceCase(GetRankingDetailPersistencePort getRankingDetailPersistencePort) {
        this.getRankingDetailPersistencePort = getRankingDetailPersistencePort;
    }

    public Optional<FetchRankingDTO> getRanking(String id, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        return Optional.ofNullable(getRankingDetailPersistencePort.getRankingDetail(id, userId));
    }
}
