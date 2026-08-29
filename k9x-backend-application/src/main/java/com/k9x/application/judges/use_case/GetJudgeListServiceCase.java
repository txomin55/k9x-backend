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

    public List<JudgeDTO> getJudges(String userId, boolean organizer, boolean created, String country) {
        AuthAssertions.assertOrganizer(organizer, userId);
        String creator = created ? userId : null;
        return getJudgeListPersistencePort.getJudges(creator, blankToNull(country)).stream()
                .map(judge -> new JudgeDTO(judge.getId(), judge.getName(), judge.getCountry()))
                .toList();
    }

    private String blankToNull(String country) {
        return country == null || country.isBlank() ? null : country;
    }
}
