package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.CreateJudgePersistencePort;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.utils.date.DateUtils;

public class CreateJudgeServiceCase {

    private final CreateJudgePersistencePort createJudgePersistencePort;

    public CreateJudgeServiceCase(CreateJudgePersistencePort createJudgePersistencePort) {
        this.createJudgePersistencePort = createJudgePersistencePort;
    }

    public void createJudge(String id, String name, String country, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        createJudgePersistencePort.createJudge(id, name, country, userId, DateUtils.nowUtcMillis());
    }
}
