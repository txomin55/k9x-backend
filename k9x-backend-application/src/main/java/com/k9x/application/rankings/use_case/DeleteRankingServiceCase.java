package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.rankings.aggregates.Ranking;

public class DeleteRankingServiceCase implements TransactionalUseCase {

    private final GetRankingPersistencePort getRankingPersistencePort;
    private final DeleteRankingPersistencePort deleteRankingPersistencePort;

    public DeleteRankingServiceCase(GetRankingPersistencePort getRankingPersistencePort,
                                    DeleteRankingPersistencePort deleteRankingPersistencePort) {
        this.getRankingPersistencePort = getRankingPersistencePort;
        this.deleteRankingPersistencePort = deleteRankingPersistencePort;
    }

    public void deleteRanking(String rankingId, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        Ranking ranking = getRankingPersistencePort.getRanking(rankingId);
        RankingGuards.assertExists(ranking);
        RankingGuards.assertMutableBy(ranking, userId);
        deleteRankingPersistencePort.deleteRanking(rankingId);
    }
}
