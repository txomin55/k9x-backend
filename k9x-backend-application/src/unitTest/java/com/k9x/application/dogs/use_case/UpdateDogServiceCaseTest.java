package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.domain.aggregates.dogs.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDogServiceCaseTest {

    @Mock
    private GetDogPersistencePort getDogPersistencePort;

    @Mock
    private UpdateDogPersistencePort updateDogPersistencePort;

    private UpdateDogServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateDogServiceCase(getDogPersistencePort, updateDogPersistencePort);
    }

    @Test
    void throws_exception_when_dog_not_found() {
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateDog("dog-1", "Rex", "img", "Lab", "id", "user-1", "user-1", "team", "ES", false))
                .isInstanceOf(DogNotFoundException.class);

        verifyNoInteractions(updateDogPersistencePort);
    }

    @Test
    void throws_exception_when_dog_is_deleted() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", "user-1", "creator-1", "ES", "team", 0L, 0L, 1700000000000L);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.updateDog("dog-1", "Rex", "img", "Lab", "id", "user-1", "user-1", "team", "ES", false))
                .isInstanceOf(DogAlreadyDeletedException.class);

        verifyNoInteractions(updateDogPersistencePort);
    }

    @Test
    void throws_exception_when_owner_does_not_match_user() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", "other-user", "creator-1", "ES", "team", 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.updateDog("dog-1", "Rex", "img", "Lab", "id", "user-1", "user-1", "team", "ES", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateDogPersistencePort);
    }

    @Test
    void throws_exception_when_no_owner_and_not_organizer() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", null, "creator-1", "ES", "team", 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.updateDog("dog-1", "Rex", "img", "Lab", "id", null, "creator-1", "team", "ES", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateDogPersistencePort);
    }

    @Test
    void throws_exception_when_no_owner_and_user_is_not_creator() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", null, "creator-1", "ES", "team", 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.updateDog("dog-1", "Rex", "img", "Lab", "id", null, "user-1", "team", "ES", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateDogPersistencePort);
    }

    @Test
    void updates_dog_by_owner_when_all_validations_pass() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", "user-1", "creator-1", "ES", "team", 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        serviceCase.updateDog("dog-1", "NewName", "img", "Lab", "id", "user-1", "user-1", "team", "ES", false);

        verify(updateDogPersistencePort).updateDog(eq("dog-1"), eq("NewName"), eq("img"), eq("Lab"), eq("id"), eq("user-1"), eq("team"), eq("ES"), anyLong());
    }

    @Test
    void updates_dog_by_organizer_creator_when_no_owner() {
        Dog dog = new Dog("dog-1", "id", "breed", "Rex", "img", null, "user-1", "ES", "team", 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        serviceCase.updateDog("dog-1", "NewName", "img", "Lab", "id", null, "user-1", "team", "ES", true);

        verify(updateDogPersistencePort).updateDog(eq("dog-1"), eq("NewName"), eq("img"), eq("Lab"), eq("id"), isNull(), eq("team"), eq("ES"), anyLong());
    }
}
