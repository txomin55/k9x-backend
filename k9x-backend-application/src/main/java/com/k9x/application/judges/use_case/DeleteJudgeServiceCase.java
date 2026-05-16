package com.k9x.application.judges.use_case;

import com.k9x.application.judges.exceptions.JudgeAlreadyDeletedException;
import com.k9x.application.judges.exceptions.JudgeNotFoundException;
import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.domain.aggregates.judges.Judge;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteJudgeServiceCase {

    private final GetJudgePersistencePort getJudgePersistencePort;
    private final DeleteJudgePersistencePort deleteJudgePersistencePort;

    public DeleteJudgeServiceCase(GetJudgePersistencePort getJudgePersistencePort,
                                  DeleteJudgePersistencePort deleteJudgePersistencePort) {
        this.getJudgePersistencePort = getJudgePersistencePort;
        this.deleteJudgePersistencePort = deleteJudgePersistencePort;
    }

    public void deleteJudge(String judgeId, String userId, boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }

        Judge judge = getJudgePersistencePort.getJudge(judgeId);
        if (judge == null) {
            throw new JudgeNotFoundException();
        }
        if (!judge.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
        if (judge.deletedAt() != null) {
            throw new JudgeAlreadyDeletedException();
        }

        deleteJudgePersistencePort.deleteJudge(judgeId, DateUtils.nowUtcMillis());
    }
}
