package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.application.judges.use_case.dto.JudgeDTO;
import com.k9x.application.utils.auth.AuthAssertions;

import java.util.List;

public class GetJudgeListServiceCase {

    private final GetJudgeListPersistencePort getJudgeListPersistencePort;

    public GetJudgeListServiceCase(GetJudgeListPersistencePort getJudgeListPersistencePort) {
        this.getJudgeListPersistencePort = getJudgeListPersistencePort;
    }

    public List<JudgeDTO> getJudges(String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        return getJudgeListPersistencePort.getJudges(userId).stream()
                .map(judge -> new JudgeDTO(judge.getId(), judge.getName(), judge.getCountry()))
                .toList();
    }
}
