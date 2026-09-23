package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogParticipationsPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;
import com.k9x.application.dogs.use_case.dto.DogParticipationYearDTO;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDogParticipationsServiceCaseTest {

    private static final String DOG_ID = "981098106001010";

    private static final Dog REX = new Dog(DOG_ID, "LOE-1234", "LIC-9", "BORDER_COLLIE", "Rex",
            "rex.png", "owner@k9x.com", "Ana", "creator@k9x.com", "ES", "Team K9X", Sex.MALE, 52, true,
            2_000L, 1_000L, null);

    @Mock
    private GetDogPersistencePort getDogPersistencePort;
    @Mock
    private GetDogParticipationsPersistencePort getDogParticipationsPersistencePort;

    private GetDogParticipationsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDogParticipationsServiceCase(getDogPersistencePort, getDogParticipationsPersistencePort);
    }

    @Test
    void throws_exception_when_dog_not_found() {
        when(getDogPersistencePort.getDog("missing")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getParticipations("missing"))
                .isInstanceOf(DogNotFoundException.class);
        verifyNoInteractions(getDogParticipationsPersistencePort);
    }

    @Test
    void returns_no_years_when_the_dog_never_competed() {
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogParticipationsPersistencePort.getParticipations(DOG_ID)).thenReturn(List.of());

        assertThat(serviceCase.getParticipations(DOG_ID)).isEmpty();
    }

    @Test
    void groups_by_utc_year_newest_year_first_and_newest_event_first() {
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogParticipationsPersistencePort.getParticipations(DOG_ID)).thenReturn(List.of(
                participation("e-2024-mar", at(2024, 3, 10, 9)),
                participation("e-2025-jan", at(2025, 1, 5, 9)),
                participation("e-2024-oct", at(2024, 10, 2, 9)),
                participation("e-2025-jun", at(2025, 6, 20, 9))));

        List<DogParticipationYearDTO> years = serviceCase.getParticipations(DOG_ID);

        assertThat(years).extracting(DogParticipationYearDTO::year).containsExactly(2025, 2024);
        assertThat(years.get(0).participations()).extracting(DogParticipationDTO::eventId)
                .containsExactly("e-2025-jun", "e-2025-jan");
        assertThat(years.get(1).participations()).extracting(DogParticipationDTO::eventId)
                .containsExactly("e-2024-oct", "e-2024-mar");
    }

    /** The year is the stage's UTC day, like every other day-based rule in k9x, not the server's zone. */
    @Test
    void assigns_the_year_by_utc_day() {
        when(getDogPersistencePort.getDog(DOG_ID)).thenReturn(REX);
        when(getDogParticipationsPersistencePort.getParticipations(DOG_ID)).thenReturn(List.of(
                participation("new-year-eve", at(2024, 12, 31, 23))));

        assertThat(serviceCase.getParticipations(DOG_ID)).extracting(DogParticipationYearDTO::year)
                .containsExactly(2024);
    }

    private static long at(int year, int month, int day, int hour) {
        return LocalDateTime.of(year, month, day, hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private static DogParticipationDTO participation(String eventId, long stageDateFrom) {
        return new DogParticipationDTO(eventId, eventId, "stage-" + eventId, stageDateFrom, "ES",
                null, null, null, false);
    }
}
