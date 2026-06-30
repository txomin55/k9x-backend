package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.application.judges.use_case.dto.JudgeDTO;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

import java.util.List;

public class GetJudgeListServiceCase {

    private final GetJudgeListPersistencePort getJudgeListPersistencePort;

    public GetJudgeListServiceCase(GetJudgeListPersistencePort getJudgeListPersistencePort) {
        this.getJudgeListPersistencePort = getJudgeListPersistencePort;
    }

    public List<JudgeDTO> getJudges(String userId, boolean organizer) {
        assertOrganizer(organizer, userId);
        return getJudgeListPersistencePort.getJudges(userId).stream()
                .map(judge -> new JudgeDTO(judge.getId(), judge.getName()))
                .toList();
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
