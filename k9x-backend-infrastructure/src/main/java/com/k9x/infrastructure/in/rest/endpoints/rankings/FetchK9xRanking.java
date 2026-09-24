package com.k9x.infrastructure.in.rest.endpoints.rankings;

import com.k9x.application.dogs.rank.use_case.GetDogRankingServiceCase;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingHighlightDTO;
import com.k9x.oas.stub.api.K9xFetchRankingApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.K9xRankingBucketResponseDTO;
import com.k9x.oas.stub.model.K9xRankingHighlightResponseDTO;
import com.k9x.oas.stub.model.K9xRankingResponseDTO;
import org.springframework.http.ResponseEntity;

/**
 * Public K9X index world ranking chart. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}.
 */
public class FetchK9xRanking implements K9xFetchRankingApiDelegate {

    private final GetDogRankingServiceCase getDogRankingServiceCase;

    public FetchK9xRanking(GetDogRankingServiceCase getDogRankingServiceCase) {
        this.getDogRankingServiceCase = getDogRankingServiceCase;
    }

    @Override
    public ResponseEntity<K9xRankingResponseDTO> fetchK9xRanking(String country, String dog) {
        DogRankingDTO ranking = getDogRankingServiceCase.getRanking(country, dog);
        return ResponseEntity.ok(new K9xRankingResponseDTO(
                ranking.total(),
                ranking.asOf(),
                ranking.buckets().stream()
                        .map(bucket -> new K9xRankingBucketResponseDTO(bucket.from(), bucket.to(), bucket.dogs()))
                        .toList(),
                ranking.highlight() == null ? null : toHighlight(ranking.highlight())));
    }

    private static K9xRankingHighlightResponseDTO toHighlight(DogRankingHighlightDTO highlight) {
        return new K9xRankingHighlightResponseDTO(
                new IdNameDTO(highlight.name(), highlight.dogIdentification()),
                highlight.index(),
                highlight.position(),
                highlight.topPercent());
    }
}
