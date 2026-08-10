package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.SaveRankingServiceCase;
import com.k9x.application.rankings.use_case.command.SaveRankingCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.oas.stub.api.SecuredRankingsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateRankingRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class CreateRanking implements SecuredRankingsCreateApiDelegate {

    private final SaveRankingServiceCase saveRankingServiceCase;
    private final UserInfoDTO userDetails;

    public CreateRanking(SaveRankingServiceCase saveRankingServiceCase, UserInfoDTO userDetails) {
        this.saveRankingServiceCase = saveRankingServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createRankingSecured(CreateRankingRequestDTO body) {
        // The criteria are resolved here so an unknown value surfaces as a domain error instead of
        // travelling down the stack as a raw string.
        saveRankingServiceCase.saveRanking(
                new SaveRankingCommand(
                        body.getRankingId(),
                        body.getName(),
                        body.getEventIds() == null ? List.of() : body.getEventIds(),
                        RankingGroupBy.from(body.getGroupBy()),
                        RankingIncludeBy.from(body.getIncludeBy()),
                        body.getIncludedCount(),
                        // Absent means reserves count, which is the column default.
                        !Boolean.FALSE.equals(body.getIncludeReserves())),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
