package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.shared.TransactionalUseCase;

public class DeleteJudgeServiceCase implements TransactionalUseCase {

    private final GetJudgePersistencePort getJudgePersistencePort;
    private final DeleteJudgePersistencePort deleteJudgePersistencePort;

    public DeleteJudgeServiceCase(GetJudgePersistencePort getJudgePersistencePort,
                                  DeleteJudgePersistencePort deleteJudgePersistencePort) {
        this.getJudgePersistencePort = getJudgePersistencePort;
        this.deleteJudgePersistencePort = deleteJudgePersistencePort;
    }

    public void deleteJudge(String judgeId, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        Judge judge = getJudgePersistencePort.getJudge(judgeId);
        JudgeGuards.assertMutableBy(judge, userId);
        deleteJudgePersistencePort.deleteJudge(judgeId, DateUtils.nowUtcMillis());
    }
}
