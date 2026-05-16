package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.dto.DogDTO;
import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizerException;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.domain.aggregates.dogs.Dog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDogListServiceCaseTest {

    @Mock
    private GetDogListPersistencePort getDogListPersistencePort;

    private GetDogListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDogListServiceCase(getDogListPersistencePort);
    }

    @Test
    void throws_exception_when_no_owner_is_provided_and_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getDogs(null, false, false))
                .isInstanceOf(OwnerNonProvidedWhenOrganizerException.class);
    }

    @Test
    void filters_dogs_to_owned_when_not_organizer() {
        when(getDogListPersistencePort.getDogs("user-1")).thenReturn(List.of());

        serviceCase.getDogs("user-1", false, false);

        verify(getDogListPersistencePort).getDogs("user-1");
    }

    @Test
    void filters_dogs_to_owned_when_organizer_and_only_owned() {
        when(getDogListPersistencePort.getDogs("user-1")).thenReturn(List.of());

        serviceCase.getDogs("user-1", true, true);

        verify(getDogListPersistencePort).getDogs("user-1");
    }

    @Test
    void fetches_all_dogs_when_organizer_and_not_only_owned() {
        when(getDogListPersistencePort.getDogs(null)).thenReturn(List.of());

        serviceCase.getDogs("user-1", true, false);

        verify(getDogListPersistencePort).getDogs(null);
    }

    @Test
    void maps_dog_to_dto_with_owned_flag() {
        Dog ownDog = new Dog("id-1", "ident-1", "breed", "Rex", "img.png", "user-1", "creator-1", "ES", "team-1", 0L, 0L, null);
        Dog othersDog = new Dog("id-2", "ident-2", "breed", "Max", "img2.png", "user-2", "creator-2", "FR", "team-2", 0L, 0L, null);
        when(getDogListPersistencePort.getDogs(null)).thenReturn(List.of(ownDog, othersDog));

        List<DogDTO> result = serviceCase.getDogs("user-1", true, false);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).owned()).isTrue();
        assertThat(result.get(1).owned()).isFalse();
    }

    @Test
    void maps_dog_fields_to_dto_correctly() {
        Dog dog = new Dog("id-1", "ident-1", "breed", "Rex", "img.png", "user-1", "creator-1", "ES", "team-1", 0L, 0L, null);
        when(getDogListPersistencePort.getDogs("user-1")).thenReturn(List.of(dog));

        List<DogDTO> result = serviceCase.getDogs("user-1", false, false);

        DogDTO dto = result.getFirst();
        assertThat(dto.id()).isEqualTo("id-1");
        assertThat(dto.name()).isEqualTo("Rex");
        assertThat(dto.image()).isEqualTo("img.png");
        assertThat(dto.owner()).isEqualTo("user-1");
        assertThat(dto.identity()).isEqualTo("ident-1");
        assertThat(dto.creator()).isEqualTo("creator-1");
        assertThat(dto.country()).isEqualTo("ES");
        assertThat(dto.team()).isEqualTo("team-1");
    }
}
