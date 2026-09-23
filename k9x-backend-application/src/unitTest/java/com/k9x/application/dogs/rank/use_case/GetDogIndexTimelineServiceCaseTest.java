package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogIndexEventsPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexEventDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexTimelineDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.dogs.rank.DogRankIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDogIndexTimelineServiceCaseTest {

    private static final String DOG_ID = "981098106001010";
    private static final long JAN_2025 = 1_735_689_600_000L;

    private static final Dog REX = new Dog(DOG_ID, "LOE-1234", "LIC-9", "BORDER_COLLIE", "Rex",
            "rex.png", "owner@k9x.com", "Ana", "creator@k9x.com", "ES", "Team K9X", Sex.MALE, 52, true,
            2_000L, 1_000L, null);

    @Mock
    private GetDogPersistencePort getDogPersistencePort;
    @Mock
    private GetDogIndexEventsPersistencePort getDogIndexEventsPersistencePort;

    private GetDogIndexTimelineServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDogIndexTimelineServiceCase(getDogPersistencePort, getDogIndexEventsPersistencePort);
    }

    @Test
    void throws_exception_when_dog_not_found() {
        when(getDogPersistencePort.getDog("missing")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getIndexTimeline("missing")).isInstanceOf(DogNotFoundException.class);
        verifyNoInteractions(getDogIndexEventsPersistencePort);
    }

    @Test
    void returns_an_empty_chart_when_the_dog_never_competed() {
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogIndexEventsPersistencePort.getIndexEvents(DOG_ID)).thenReturn(List.of());

        assertThat(serviceCase.getIndexTimeline(DOG_ID))
                .isEqualTo(new DogIndexTimelineDTO(List.of(), List.of(), null));
    }

    @Test
    void orders_the_events_and_computes_the_index_after_each_one() {
        FetchDogIndexEventDTO second = result("e-2", DogRankIndex.plusMonths(JAN_2025, 5), "600");
        FetchDogIndexEventDTO first = result("e-1", JAN_2025, "750");
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogIndexEventsPersistencePort.getIndexEvents(DOG_ID)).thenReturn(List.of(second, first));

        DogIndexTimelineDTO timeline = serviceCase.getIndexTimeline(DOG_ID);

        assertThat(timeline.events()).extracting(event -> event.result().eventId()).containsExactly("e-1", "e-2");
        assertThat(timeline.events()).extracting(DogIndexEventDTO::indexAfter).containsExactly(
                DogRankIndex.of(List.of(history(first)), first.appliesAt()),
                DogRankIndex.of(List.of(history(first), history(second)), second.appliesAt()));
    }

    /** The chart draws where the whole index starts fading if the dog does not compete again. */
    @Test
    void marks_where_the_whole_index_starts_fading() {
        FetchDogIndexEventDTO first = result("e-1", JAN_2025, "750");
        FetchDogIndexEventDTO last = result("e-2", DogRankIndex.plusMonths(JAN_2025, 5), "600");
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogIndexEventsPersistencePort.getIndexEvents(DOG_ID)).thenReturn(List.of(first, last));

        DogIndexTimelineDTO timeline = serviceCase.getIndexTimeline(DOG_ID);

        assertThat(timeline.freshnessDegradationFrom()).isEqualTo(DogRankIndex.plusMonths(last.appliesAt(), 6));
        assertThat(timeline.curve()).isNotEmpty();
        assertThat(timeline.curve().get(0).timestamp()).isEqualTo(JAN_2025);
    }

    private static DogRankIndex.Result history(FetchDogIndexEventDTO result) {
        return new DogRankIndex.Result(result.rankScore(), result.appliesAt());
    }

    private static FetchDogIndexEventDTO result(String eventId, long appliesAt, String rankScore) {
        return new FetchDogIndexEventDTO(eventId, eventId, "stage-" + eventId, "OBDX", "ES", appliesAt,
                new BigDecimal(rankScore), (short) 1, new BigDecimal("280.00"), false);
    }
}
