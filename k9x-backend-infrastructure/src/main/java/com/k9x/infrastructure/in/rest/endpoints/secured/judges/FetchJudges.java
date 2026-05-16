package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.GetJudgeListServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredJudgesFetchAllApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchJudges implements SecuredJudgesFetchAllApiDelegate {

    private final GetJudgeListServiceCase getJudgeListServiceCase;
    private final UserInfoDTO userDetails;

    public FetchJudges(GetJudgeListServiceCase getJudgeListServiceCase, UserInfoDTO userDetails) {
        this.getJudgeListServiceCase = getJudgeListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchJudgesSecured() {
        return ResponseEntity.ok(
                getJudgeListServiceCase.getJudges(userDetails.getEmail(), userDetails.isOrganizer()).stream()
                        .map(judge -> new IdNameDTO(judge.name(), judge.id()))
                        .toList()
        );
    }
}
