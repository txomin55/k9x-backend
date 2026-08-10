package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.GetRankingIncludeByListServiceCase;
import com.k9x.oas.stub.api.SecuredRankingsFetchIncludeBysApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchRankingIncludeBys implements SecuredRankingsFetchIncludeBysApiDelegate {

    private final GetRankingIncludeByListServiceCase getRankingIncludeByListServiceCase;

    public FetchRankingIncludeBys(GetRankingIncludeByListServiceCase getRankingIncludeByListServiceCase) {
        this.getRankingIncludeByListServiceCase = getRankingIncludeByListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchRankingIncludeBysSecured() {
        return ResponseEntity.ok(
                getRankingIncludeByListServiceCase.getIncludeBys().stream()
                        .map(criterion -> new IdNameDTO(criterion.name(), criterion.id()))
                        .toList());
    }
}
