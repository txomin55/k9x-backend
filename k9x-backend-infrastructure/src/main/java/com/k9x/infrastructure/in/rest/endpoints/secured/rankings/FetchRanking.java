package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.GetRankingServiceCase;
import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredRankingsFetchOneApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.RankingResponseDTO;
import org.springframework.http.ResponseEntity;

public class FetchRanking implements SecuredRankingsFetchOneApiDelegate {

    private final GetRankingServiceCase getRankingServiceCase;
    private final UserInfoDTO userDetails;

    public FetchRanking(GetRankingServiceCase getRankingServiceCase, UserInfoDTO userDetails) {
        this.getRankingServiceCase = getRankingServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<RankingResponseDTO> fetchRankingSecured(String id) {
        return getRankingServiceCase
                .getRanking(id, userDetails.getEmail(), userDetails.isOrganizer())
                .map(FetchRanking::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private static RankingResponseDTO toResponse(FetchRankingDTO ranking) {
        return new RankingResponseDTO(
                ranking.id(),
                ranking.name(),
                ranking.events().stream()
                        .map(event -> new IdNameDTO(event.name(), event.id()))
                        .toList(),
                ranking.groupBy(),
                ranking.includeBy(),
                ranking.includedCount(),
                ranking.includeReserves());
    }
}
