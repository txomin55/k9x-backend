package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.DeleteRankingServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredRankingsRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveRanking implements SecuredRankingsRemoveApiDelegate {

    private final DeleteRankingServiceCase deleteRankingServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveRanking(DeleteRankingServiceCase deleteRankingServiceCase, UserInfoDTO userDetails) {
        this.deleteRankingServiceCase = deleteRankingServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeRankingSecured(String id) {
        deleteRankingServiceCase.deleteRanking(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
