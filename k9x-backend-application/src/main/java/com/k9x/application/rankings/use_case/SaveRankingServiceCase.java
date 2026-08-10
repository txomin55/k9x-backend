package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetActiveEventIdsPersistencePort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.port.SaveRankingPersistencePort;
import com.k9x.application.rankings.port.payload.SaveRankingPersistencePayload;
import com.k9x.application.rankings.use_case.command.SaveRankingCommand;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.rankings.aggregates.Ranking;

/**
 * Stores a ranking. The identifier comes from the client, so this is an upsert: an existing ranking is
 * physically replaced rather than updated, which is why the delete and the insert run inside the same
 * transaction (guaranteed by {@link TransactionalUseCase}).
 */
public class SaveRankingServiceCase implements TransactionalUseCase {

    private final GetRankingPersistencePort getRankingPersistencePort;
    private final GetActiveEventIdsPersistencePort getActiveEventIdsPersistencePort;
    private final SaveRankingPersistencePort saveRankingPersistencePort;
    private final DeleteRankingPersistencePort deleteRankingPersistencePort;

    public SaveRankingServiceCase(GetRankingPersistencePort getRankingPersistencePort,
                                  GetActiveEventIdsPersistencePort getActiveEventIdsPersistencePort,
                                  SaveRankingPersistencePort saveRankingPersistencePort,
                                  DeleteRankingPersistencePort deleteRankingPersistencePort) {
        this.getRankingPersistencePort = getRankingPersistencePort;
        this.getActiveEventIdsPersistencePort = getActiveEventIdsPersistencePort;
        this.saveRankingPersistencePort = saveRankingPersistencePort;
        this.deleteRankingPersistencePort = deleteRankingPersistencePort;
    }

    public void saveRanking(SaveRankingCommand command, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        RankingGuards.assertValidConfiguration(command);
        // Validated before reading the existing row so an invalid request never costs a second query and
        // the exception order stays deterministic.
        RankingGuards.assertEventsAreActive(command.eventIds(),
                getActiveEventIdsPersistencePort.getActiveEventIds(command.eventIds()));

        Ranking existing = getRankingPersistencePort.getRanking(command.rankingId());
        RankingGuards.assertMutableBy(existing, userId);
        if (existing != null) {
            deleteRankingPersistencePort.deleteRanking(command.rankingId());
        }
        saveRankingPersistencePort.saveRanking(SaveRankingPersistencePayload.from(command, userId));
    }
}
