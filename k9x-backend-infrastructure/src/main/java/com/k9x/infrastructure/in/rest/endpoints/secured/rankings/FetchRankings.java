package com.k9x.infrastructure.in.rest.endpoints.secured.rankings;

import com.k9x.application.rankings.use_case.GetRankingListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredRankingsFetchAllApiDelegate;
import com.k9x.oas.stub.model.RankingListItemResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchRankings implements SecuredRankingsFetchAllApiDelegate {

    private final GetRankingListServiceCase getRankingListServiceCase;
    private final UserInfoDTO userDetails;

    public FetchRankings(GetRankingListServiceCase getRankingListServiceCase, UserInfoDTO userDetails) {
        this.getRankingListServiceCase = getRankingListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<RankingListItemResponseDTO>> fetchRankingsSecured() {
        return ResponseEntity.ok(
                getRankingListServiceCase
                        .getRankings(userDetails.getEmail(), userDetails.isOrganizer()).stream()
                        .map(ranking -> new RankingListItemResponseDTO(
                                ranking.id(),
                                ranking.name(),
                                ranking.eventCount(),
                                ranking.groupBy(),
                                ranking.includeBy(),
                                ranking.includedCount(),
                                ranking.includeReserves()))
                        .toList());
    }
}
