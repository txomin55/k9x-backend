package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.GetJudgeListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
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
        // TODO: judge.country() is now available from the service, but the published oas-definition-stubs jar
        //  still returns IdNameDTO here (no country). Once the stub is republished, return JudgeResponseDTO
        //  (name, id, country) as declared in oas.yaml and map judge.country().
        return ResponseEntity.ok(
                getJudgeListServiceCase.getJudges(userDetails.getEmail(), userDetails.isOrganizer()).stream()
                        .map(judge -> new IdNameDTO(judge.name(), judge.id()))
                        .toList()
        );
    }
}
