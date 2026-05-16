package com.k9x.application.judges.use_case;

import com.k9x.application.judges.exceptions.JudgeNotFoundException;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.application.judges.port.UpdateJudgePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.judges.Judge;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateJudgeServiceCase {

    private final GetJudgePersistencePort getJudgePersistencePort;
    private final UpdateJudgePersistencePort updateJudgePersistencePort;

    public UpdateJudgeServiceCase(GetJudgePersistencePort getJudgePersistencePort,
                                  UpdateJudgePersistencePort updateJudgePersistencePort) {
        this.getJudgePersistencePort = getJudgePersistencePort;
        this.updateJudgePersistencePort = updateJudgePersistencePort;
    }

    public void updateJudge(String judgeId, String name, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Judge judge = getJudgePersistencePort.getJudge(judgeId);
        assertJudgeExists(judge);
        assertUserIsJudgeCreator(judge, userId);
        updateJudgePersistencePort.updateJudge(judgeId, name, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertJudgeExists(Judge judge) {
        if (judge == null) {
            throw new JudgeNotFoundException();
        }
    }

    private void assertUserIsJudgeCreator(Judge judge, String userId) {
        if (!judge.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
