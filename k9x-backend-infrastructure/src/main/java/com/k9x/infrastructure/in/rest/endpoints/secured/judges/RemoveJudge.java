package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.DeleteJudgeServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredJudgesRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveJudge implements SecuredJudgesRemoveApiDelegate {

    private final DeleteJudgeServiceCase deleteJudgeServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveJudge(DeleteJudgeServiceCase deleteJudgeServiceCase, UserInfoDTO userDetails) {
        this.deleteJudgeServiceCase = deleteJudgeServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeJudgeSecured(String id) {
        deleteJudgeServiceCase.deleteJudge(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
