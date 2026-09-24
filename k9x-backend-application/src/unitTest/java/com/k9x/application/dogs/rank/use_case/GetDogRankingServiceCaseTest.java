package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.GetDogRankingDistributionPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankingEntryPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingBucketDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogRankingHighlightDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingDistributionDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingEntryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDogRankingServiceCaseTest {

    private static final long COMPUTED_AT = 1_800_000_000_000L;
    private static final String DOG_ID = "981098106001010";

    @Mock
    GetDogRankingDistributionPersistencePort getDogRankingDistributionPersistencePort;
    @Mock
    GetDogRankingEntryPersistencePort getDogRankingEntryPersistencePort;

    private GetDogRankingServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDogRankingServiceCase(getDogRankingDistributionPersistencePort,
                getDogRankingEntryPersistencePort);
    }

    private void field(String country, Map<Integer, Integer> dogsByIndex) {
        when(getDogRankingDistributionPersistencePort.getDistribution(country, 100))
                .thenReturn(new FetchDogRankingDistributionDTO(COMPUTED_AT, dogsByIndex));
    }

    @Test
    void charts_the_field_in_bands_from_the_threshold_without_a_highlight_when_no_dog_is_requested() {
        field(null, Map.of(953, 1, 410, 3, 100, 2));

        DogRankingDTO ranking = serviceCase.getRanking(null, null);

        assertThat(ranking.total()).isEqualTo(6);
        assertThat(ranking.asOf()).isEqualTo(COMPUTED_AT);
        assertThat(ranking.buckets()).hasSize(45);
        assertThat(ranking.buckets().get(0)).isEqualTo(new DogRankingBucketDTO(100, 120, 2));
        assertThat(ranking.buckets().get(15)).isEqualTo(new DogRankingBucketDTO(400, 420, 3));
        assertThat(ranking.highlight()).isNull();
        verifyNoInteractions(getDogRankingEntryPersistencePort);
    }

    @Test
    void answers_an_empty_chart_while_the_snapshot_has_never_been_written() {
        when(getDogRankingDistributionPersistencePort.getDistribution(null, 100))
                .thenReturn(new FetchDogRankingDistributionDTO(null, Map.of()));

        DogRankingDTO ranking = serviceCase.getRanking(null, null);

        assertThat(ranking.total()).isZero();
        assertThat(ranking.asOf()).isNull();
        assertThat(ranking.buckets()).allMatch(bucket -> bucket.dogs() == 0);
    }

    @Test
    void places_the_requested_dog_among_the_charted_dogs() {
        field(null, Map.of(990, 2, 953, 1, 410, 97));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID))
                .thenReturn(Optional.of(new FetchDogRankingEntryDTO(DOG_ID, "Rex", 953, "ES")));

        DogRankingDTO ranking = serviceCase.getRanking(null, DOG_ID);

        assertThat(ranking.highlight()).isEqualTo(new DogRankingHighlightDTO(DOG_ID, "Rex", 953, 3, 3));
    }

    @Test
    void positions_within_the_requested_country() {
        field("ES", Map.of(953, 1, 410, 1));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID))
                .thenReturn(Optional.of(new FetchDogRankingEntryDTO(DOG_ID, "Rex", 953, "ES")));

        DogRankingDTO ranking = serviceCase.getRanking("ES", DOG_ID);

        verify(getDogRankingDistributionPersistencePort).getDistribution("ES", 100);
        assertThat(ranking.highlight().position()).isEqualTo(1);
        assertThat(ranking.highlight().topPercent()).isEqualTo(50);
    }

    @Test
    void has_no_highlight_for_a_dog_of_another_country() {
        field("FR", Map.of(953, 1, 410, 1));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID))
                .thenReturn(Optional.of(new FetchDogRankingEntryDTO(DOG_ID, "Rex", 953, "ES")));

        assertThat(serviceCase.getRanking("FR", DOG_ID).highlight()).isNull();
    }

    @Test
    void has_no_highlight_for_a_dog_below_the_threshold() {
        field(null, Map.of(410, 1));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID))
                .thenReturn(Optional.of(new FetchDogRankingEntryDTO(DOG_ID, "Rex", 99, "ES")));

        DogRankingDTO ranking = serviceCase.getRanking(null, DOG_ID);

        assertThat(ranking.highlight()).isNull();
        assertThat(ranking.total()).isEqualTo(1);
    }

    @Test
    void has_no_highlight_for_a_dog_outside_the_snapshot() {
        field(null, Map.of(410, 1));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID)).thenReturn(Optional.empty());

        assertThat(serviceCase.getRanking(null, DOG_ID).highlight()).isNull();
    }

    @Test
    void has_no_highlight_when_the_dog_index_is_missing_from_the_counted_field() {
        // a cron rewrite landed between both reads: the dog's new index is not in the field it would be ranked in
        field(null, Map.of(410, 1));
        when(getDogRankingEntryPersistencePort.getEntry(DOG_ID))
                .thenReturn(Optional.of(new FetchDogRankingEntryDTO(DOG_ID, "Rex", 953, "ES")));

        assertThat(serviceCase.getRanking(null, DOG_ID).highlight()).isNull();
    }
}
