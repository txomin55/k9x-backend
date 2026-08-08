package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDogServiceCaseTest {

    @Mock
    private GetDogPersistencePort getDogPersistencePort;

    @Mock
    private DeleteDogPersistencePort deleteDogPersistencePort;

    private DeleteDogServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteDogServiceCase(getDogPersistencePort, deleteDogPersistencePort);
    }

    @Test
    void throws_exception_when_dog_not_found() {
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteDog("dog-1", "user-1", false))
                .isInstanceOf(DogNotFoundException.class);

        verifyNoInteractions(deleteDogPersistencePort);
    }

    @Test
    void throws_exception_when_owner_does_not_match_user() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", "other-user", "handler-1", "creator-1", "ES", "team", null, null, null, 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.deleteDog("dog-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteDogPersistencePort);
    }

    @Test
    void throws_exception_when_no_owner_and_not_organizer() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", null, "handler-1", "creator-1", "ES", "team", null, null, null, 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.deleteDog("dog-1", "creator-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteDogPersistencePort);
    }

    @Test
    void throws_exception_when_no_owner_and_user_is_not_creator() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", null, "handler-1", "creator-1", "ES", "team", null, null, null, 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.deleteDog("dog-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteDogPersistencePort);
    }

    @Test
    void throws_exception_when_dog_already_deleted() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", "user-1", "handler-1", "creator-1", "ES", "team", null, null, null, 0L, 0L, 1000L);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        assertThatThrownBy(() -> serviceCase.deleteDog("dog-1", "user-1", true))
                .isInstanceOf(DogAlreadyDeletedException.class);

        verifyNoInteractions(deleteDogPersistencePort);
    }

    @Test
    void deletes_dog_by_owner_when_all_validations_pass() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", "user-1", "handler-1", "creator-1", "ES", "team", null, null, null, 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        serviceCase.deleteDog("dog-1", "user-1", false);

        verify(deleteDogPersistencePort).deleteDog(eq("dog-1"), anyLong());
    }

    @Test
    void deletes_dog_by_organizer_creator_when_no_owner() {
        Dog dog = new Dog("dog-1", "id", null, "breed", "Rex", "img", null, "handler-1", "user-1", "ES", "team", null, null, null, 0L, 0L, null);
        when(getDogPersistencePort.getDog("dog-1")).thenReturn(dog);

        serviceCase.deleteDog("dog-1", "user-1", true);

        verify(deleteDogPersistencePort).deleteDog(eq("dog-1"), anyLong());
    }
}
