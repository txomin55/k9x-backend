package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.CreateJudgePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class CreateJudgeServiceCase {

    private final CreateJudgePersistencePort createJudgePersistencePort;

    public CreateJudgeServiceCase(CreateJudgePersistencePort createJudgePersistencePort) {
        this.createJudgePersistencePort = createJudgePersistencePort;
    }

    public void createJudge(String id, String name, String userId, boolean organizer) {
        assertOrganizer(organizer, userId);
        createJudgePersistencePort.createJudge(id, name, userId, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
