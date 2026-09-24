package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.GetDogRankingDistributionPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankingEntryPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingBucketDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingHighlightDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingDistributionDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingEntryDTO;
import com.k9x.domain.dogs.rank.DogRankDistribution;

import java.util.List;
import java.util.Map;

/**
 * The public world ranking chart, read from the ranking snapshot the index cron rewrites ({@code
 * k9x.snap_dog_ranking}), world-wide or within one country. Only dogs at or above
 * {@link DogRankDistribution#MIN_CHARTED_INDEX} are charted, and the total and a highlighted dog's position are
 * taken from the same per-index count as the bands, so the three always agree. Like the other public reads it
 * asserts nothing about the caller, and an unknown or uncharted dog simply has no highlight.
 */
public class GetDogRankingServiceCase {

    private final GetDogRankingDistributionPersistencePort getDogRankingDistributionPersistencePort;
    private final GetDogRankingEntryPersistencePort getDogRankingEntryPersistencePort;

    public GetDogRankingServiceCase(GetDogRankingDistributionPersistencePort getDogRankingDistributionPersistencePort,
                                    GetDogRankingEntryPersistencePort getDogRankingEntryPersistencePort) {
        this.getDogRankingDistributionPersistencePort = getDogRankingDistributionPersistencePort;
        this.getDogRankingEntryPersistencePort = getDogRankingEntryPersistencePort;
    }

    public DogRankingDTO getRanking(String country, String dogIdentification) {
        FetchDogRankingDistributionDTO distribution = getDogRankingDistributionPersistencePort
                .getDistribution(country, DogRankDistribution.MIN_CHARTED_INDEX);
        Map<Integer, Integer> dogsByIndex = distribution.dogsByIndex();

        List<DogRankingBucketDTO> buckets = DogRankDistribution.buckets(dogsByIndex).stream()
                .map(bucket -> new DogRankingBucketDTO(bucket.from(), bucket.to(), bucket.dogs()))
                .toList();
        int total = buckets.stream().mapToInt(DogRankingBucketDTO::dogs).sum();

        DogRankingHighlightDTO highlight = dogIdentification == null ? null
                : getDogRankingEntryPersistencePort.getEntry(dogIdentification)
                        .filter(entry -> onChart(entry, country, dogsByIndex))
                        .map(entry -> highlight(entry, dogsByIndex, total))
                        .orElse(null);

        return new DogRankingDTO(total, distribution.computedAt(), buckets, highlight);
    }

    /**
     * The dog is on the chart when it clears the threshold and belongs to the charted country. Its index must
     * also be one the count holds: both reads come from the same snapshot, and this keeps a cron rewrite landing
     * between them from placing the dog outside the field it is counted against.
     */
    private static boolean onChart(FetchDogRankingEntryDTO entry, String country,
                                   Map<Integer, Integer> dogsByIndex) {
        return DogRankDistribution.charted(entry.index())
                && (country == null || country.equals(entry.country()))
                && dogsByIndex.containsKey(entry.index());
    }

    private static DogRankingHighlightDTO highlight(FetchDogRankingEntryDTO entry, Map<Integer, Integer> dogsByIndex,
                                                    int total) {
        int position = DogRankDistribution.position(dogsByIndex, entry.index());
        return new DogRankingHighlightDTO(entry.dogIdentification(), entry.name(), entry.index(), position,
                DogRankDistribution.topPercent(position, total));
    }
}
