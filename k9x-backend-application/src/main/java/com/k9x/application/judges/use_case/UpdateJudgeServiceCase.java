package com.k9x.application.judges.use_case;

import com.k9x.application.judges.command.UpdateJudgeCommand;
import com.k9x.application.judges.payload.UpdateJudgePersistencePayload;
import com.k9x.application.judges.exceptions.JudgeAlreadyDeletedException;
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

    public void updateJudge(String judgeId, UpdateJudgeCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Judge judge = getJudgePersistencePort.getJudge(judgeId);
        assertJudgeValidations(judge, userId);
        updateJudgePersistencePort.updateJudge(judgeId, new UpdateJudgePersistencePayload(command.name(), DateUtils.nowUtcMillis()));
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertJudgeValidations(Judge judge, String userId) {
        if (judge == null) {
            throw new JudgeNotFoundException();
        }
        if (judge.deletedAt() != null) {
            throw new JudgeAlreadyDeletedException();
        }
        if (!judge.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
