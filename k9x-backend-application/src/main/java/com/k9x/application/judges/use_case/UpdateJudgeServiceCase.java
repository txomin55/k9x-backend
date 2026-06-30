package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.payload.UpdateJudgePersistencePayload;
import com.k9x.application.judges.use_case.command.UpdateJudgeCommand;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.application.judges.port.UpdateJudgePersistencePort;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.judges.aggregates.Judge;

public class UpdateJudgeServiceCase {

    private final GetJudgePersistencePort getJudgePersistencePort;
    private final UpdateJudgePersistencePort updateJudgePersistencePort;

    public UpdateJudgeServiceCase(GetJudgePersistencePort getJudgePersistencePort,
                                  UpdateJudgePersistencePort updateJudgePersistencePort) {
        this.getJudgePersistencePort = getJudgePersistencePort;
        this.updateJudgePersistencePort = updateJudgePersistencePort;
    }

    public void updateJudge(String judgeId, UpdateJudgeCommand command, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        Judge judge = getJudgePersistencePort.getJudge(judgeId);
        JudgeGuards.assertMutableBy(judge, userId);
        updateJudgePersistencePort.updateJudge(judgeId, UpdateJudgePersistencePayload.from(command));
    }
}
