package com.k9x.application.judges.use_case;

import com.k9x.application.judges.dto.JudgeDTO;
import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.util.List;

public class GetJudgeListServiceCase {

    private final GetJudgeListPersistencePort getJudgeListPersistencePort;

    public GetJudgeListServiceCase(GetJudgeListPersistencePort getJudgeListPersistencePort) {
        this.getJudgeListPersistencePort = getJudgeListPersistencePort;
    }

    public List<JudgeDTO> getJudges(String userId, boolean organizer) {
        assertOrganizer(organizer);
        return getJudgeListPersistencePort.getJudges(userId).stream()
                .map(judge -> new JudgeDTO(judge.getId(), judge.getName()))
                .toList();
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }
}
