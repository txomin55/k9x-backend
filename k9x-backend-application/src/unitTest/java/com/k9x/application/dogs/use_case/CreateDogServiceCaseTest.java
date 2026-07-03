package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDogServiceCaseTest {

    @Mock
    private CreateDogPersistencePort createDogPersistencePort;

    private CreateDogServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateDogServiceCase(createDogPersistencePort);
    }

    @Test
    void throws_exception_when_not_organizer_and_owner_is_null() {
        assertThatThrownBy(() -> serviceCase.createDog("dog-1", "Rex", "img", "Lab", "id", null, "handler-1", "user-1", "team", "ES", null, null, false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createDogPersistencePort);
    }

    @Test
    void throws_exception_when_not_organizer_and_owner_does_not_match_user() {
        assertThatThrownBy(() -> serviceCase.createDog("dog-1", "Rex", "img", "Lab", "id", "other-user", "handler-1", "user-1", "team", "ES", null, null, false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createDogPersistencePort);
    }

    @Test
    void creates_dog_when_not_organizer_and_owner_matches_user() {
        serviceCase.createDog("dog-1", "Rex", "img", "Lab", "id", "user-1", "handler-1", "user-1", "team", "ES", null, null, false);

        verify(createDogPersistencePort).createDog(eq("dog-1"), eq("Rex"), eq("img"), eq("Lab"), eq("id"), eq("user-1"), eq("handler-1"), eq("user-1"), eq("team"), eq("ES"), any(), any(), anyLong());
    }

    @Test
    void creates_dog_when_organizer_regardless_of_owner() {
        serviceCase.createDog("dog-1", "Rex", "img", "Lab", "id", "other-user", "handler-1", "user-1", "team", "ES", null, null, true);

        verify(createDogPersistencePort).createDog(eq("dog-1"), eq("Rex"), eq("img"), eq("Lab"), eq("id"), eq("other-user"), eq("handler-1"), eq("user-1"), eq("team"), eq("ES"), any(), any(), anyLong());
    }
}
