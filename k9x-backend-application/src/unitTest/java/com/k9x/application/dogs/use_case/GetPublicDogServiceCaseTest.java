package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.use_case.dto.PublicDogDetailDTO;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicDogServiceCaseTest {

    private static final Dog REX = new Dog("981098106001010", "LOE-1234", "LIC-9", "BORDER_COLLIE", "Rex",
            "rex.png", "owner@k9x.com", "Ana", "creator@k9x.com", "ES", "Team K9X", Sex.MALE, 52, true,
            2_000L, 1_000L, null);

    @Mock
    private GetDogPersistencePort getDogPersistencePort;

    private GetPublicDogServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetPublicDogServiceCase(getDogPersistencePort);
    }

    @Test
    void throws_exception_when_dog_not_found() {
        when(getDogPersistencePort.getDog("missing")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getDog("missing"))
                .isInstanceOf(DogNotFoundException.class);
    }

    @Test
    void returns_every_public_field_of_the_dog() {
        when(getDogPersistencePort.getDog(REX.identification())).thenReturn(REX);

        PublicDogDetailDTO dog = serviceCase.getDog(REX.identification());

        assertThat(dog.identification()).isEqualTo("981098106001010");
        assertThat(dog.name()).isEqualTo("Rex");
        assertThat(dog.image()).isEqualTo("rex.png");
        assertThat(dog.breed()).isEqualTo("BORDER_COLLIE");
        assertThat(dog.origin()).isEqualTo("LOE-1234");
        assertThat(dog.license()).isEqualTo("LIC-9");
        assertThat(dog.country()).isEqualTo("ES");
        assertThat(dog.team()).isEqualTo("Team K9X");
        assertThat(dog.handler()).isEqualTo("Ana");
        assertThat(dog.sex()).isEqualTo(Sex.MALE);
        assertThat(dog.withersCm()).isEqualTo(52);
        assertThat(dog.threeFciGenerationsConfirmed()).isTrue();
        assertThat(dog.lastUpdate()).isEqualTo(2_000L);
    }

    /**
     * The owner, the creator and the creation date say something about the app's users rather than about
     * the dog, so no anonymous reader gets to see them: the DTO has nowhere to carry them.
     */
    @Test
    void exposes_neither_the_owner_nor_the_creator_nor_the_creation_date() {
        assertThat(PublicDogDetailDTO.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("owner", "creator", "createdAt", "deletedAt");
    }
}
