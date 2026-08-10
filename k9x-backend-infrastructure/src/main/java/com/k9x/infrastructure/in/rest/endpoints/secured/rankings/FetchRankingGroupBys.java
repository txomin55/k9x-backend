package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.GetRankingGroupByListServiceCase;
import com.k9x.oas.stub.api.SecuredRankingsFetchGroupBysApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchRankingGroupBys implements SecuredRankingsFetchGroupBysApiDelegate {

    private final GetRankingGroupByListServiceCase getRankingGroupByListServiceCase;

    public FetchRankingGroupBys(GetRankingGroupByListServiceCase getRankingGroupByListServiceCase) {
        this.getRankingGroupByListServiceCase = getRankingGroupByListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchRankingGroupBysSecured() {
        return ResponseEntity.ok(
                getRankingGroupByListServiceCase.getGroupBys().stream()
                        .map(criterion -> new IdNameDTO(criterion.name(), criterion.id()))
                        .toList());
    }
}
