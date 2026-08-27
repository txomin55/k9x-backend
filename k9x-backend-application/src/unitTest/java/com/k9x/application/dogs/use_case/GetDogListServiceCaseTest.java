package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizerException;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.payload.DogListFilter;
import com.k9x.application.dogs.port.payload.DogListPage;
import com.k9x.application.dogs.use_case.command.GetDogListCommand;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.application.dogs.use_case.dto.DogListDTO;
import com.k9x.domain.dogs.aggregates.Dog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDogListServiceCaseTest {

    @Mock
    private GetDogListPersistencePort getDogListPersistencePort;

    @Captor
    private ArgumentCaptor<DogListFilter> filterCaptor;

    private GetDogListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDogListServiceCase(getDogListPersistencePort);
    }

    private void givenDogs(Dog... dogs) {
        when(getDogListPersistencePort.getDogs(any())).thenReturn(new DogListPage(List.of(dogs), dogs.length));
    }

    private DogListFilter capturedFilter() {
        verify(getDogListPersistencePort).getDogs(filterCaptor.capture());
        return filterCaptor.getValue();
    }

    @Test
    void throws_exception_when_no_owner_is_provided_and_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getDogs(null, false, GetDogListCommand.ALL))
                .isInstanceOf(OwnerNonProvidedWhenOrganizerException.class);
    }

    @Test
    void filters_dogs_to_own_when_not_organizer_and_no_filter() {
        givenDogs();

        serviceCase.getDogs("user-1", false, GetDogListCommand.ALL);

        assertThat(capturedFilter().owner()).isEqualTo("user-1");
        assertThat(capturedFilter().creator()).isEqualTo("user-1");
    }

    @Test
    void filters_dogs_to_owned_when_owned_requested() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(true, false, null, null, null));

        assertThat(capturedFilter().owner()).isEqualTo("user-1");
        assertThat(capturedFilter().creator()).isNull();
    }

    @Test
    void filters_dogs_to_created_when_created_requested() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(false, true, null, null, null));

        assertThat(capturedFilter().owner()).isNull();
        assertThat(capturedFilter().creator()).isEqualTo("user-1");
    }

    @Test
    void merges_owned_and_created_when_both_requested() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(true, true, null, null, null));

        assertThat(capturedFilter().owner()).isEqualTo("user-1");
        assertThat(capturedFilter().creator()).isEqualTo("user-1");
    }

    @Test
    void fetches_all_dogs_when_organizer_and_no_filter() {
        givenDogs();

        serviceCase.getDogs("user-1", true, GetDogListCommand.ALL);

        assertThat(capturedFilter().owner()).isNull();
        assertThat(capturedFilter().creator()).isNull();
    }

    // ---- name filter ------------------------------------------------------------------------------

    @Test
    void passes_the_name_search_to_the_persistence_filter() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, " Rex ", null, null));

        assertThat(capturedFilter().nameContains()).isEqualTo("Rex");
    }

    @Test
    void ignores_a_blank_name_search() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, "   ", null, null));

        assertThat(capturedFilter().nameContains()).isNull();
    }

    // ---- pagination -------------------------------------------------------------------------------

    @Test
    void does_not_paginate_when_no_size_is_requested() {
        Dog dog = dog("id-1", "user-1", "creator-1");
        givenDogs(dog);

        DogListDTO result = serviceCase.getDogs("user-1", true, GetDogListCommand.ALL);

        assertThat(capturedFilter().paginated()).isFalse();
        assertThat(capturedFilter().limit()).isNull();
        assertThat(capturedFilter().offset()).isNull();
        // The whole list comes back as a single page.
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void translates_page_and_size_into_offset_and_limit() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, null, 3, 20));

        assertThat(capturedFilter().offset()).isEqualTo(60);
        assertThat(capturedFilter().limit()).isEqualTo(20);
    }

    @Test
    void defaults_to_the_first_page_when_only_size_is_requested() {
        givenDogs();

        serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, null, null, 20));

        assertThat(capturedFilter().offset()).isZero();
        assertThat(capturedFilter().limit()).isEqualTo(20);
    }

    @Test
    void reports_the_page_window_and_the_total_across_pages() {
        when(getDogListPersistencePort.getDogs(any()))
                .thenReturn(new DogListPage(List.of(dog("id-1", "user-1", "creator-1")), 137));

        DogListDTO result = serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, null, 2, 20));

        assertThat(result.items()).hasSize(1);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.total()).isEqualTo(137);
        // 137 dogs at 20 per page -> the last page is a partial one.
        assertThat(result.totalPages()).isEqualTo(7);
    }

    @Test
    void reports_no_pages_when_nothing_matches() {
        when(getDogListPersistencePort.getDogs(any())).thenReturn(new DogListPage(List.of(), 0));

        DogListDTO result = serviceCase.getDogs("user-1", true, new GetDogListCommand(false, false, "nope", 0, 20));

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    // ---- mapping ----------------------------------------------------------------------------------

    @Test
    void maps_dog_to_dto_with_owned_flag() {
        Dog ownDog = new Dog("id-1", "ident-1", null, "breed", "Rex", "img.png", "user-1", "handler-1", "creator-1", "ES", "team-1", null, null, null, 0L, 0L, null);
        Dog othersDog = new Dog("id-2", "ident-2", null, "breed", "Max", "img2.png", "user-2", "handler-1", "creator-2", "FR", "team-2", null, null, null, 0L, 0L, null);
        givenDogs(ownDog, othersDog);

        List<DogDTO> result = serviceCase.getDogs("user-1", true, GetDogListCommand.ALL).items();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).owned()).isTrue();
        assertThat(result.get(1).owned()).isFalse();
    }

    @Test
    void marks_dog_as_owned_when_owner_is_null_and_user_is_creator() {
        Dog createdDog = new Dog("id-1", "ident-1", null, "breed", "Rex", "img.png", null, "handler-1", "user-1", "ES", "team-1", null, null, null, 0L, 0L, null);
        Dog othersCreatedDog = new Dog("id-2", "ident-2", null, "breed", "Max", "img2.png", null, "handler-1", "creator-2", "FR", "team-2", null, null, null, 0L, 0L, null);
        givenDogs(createdDog, othersCreatedDog);

        List<DogDTO> result = serviceCase.getDogs("user-1", true, new GetDogListCommand(false, true, null, null, null)).items();

        assertThat(result.get(0).owned()).isTrue();
        assertThat(result.get(1).owned()).isFalse();
    }

    @Test
    void does_not_mark_dog_as_owned_by_creator_when_owner_is_set_to_another_user() {
        Dog dog = new Dog("id-1", "ident-1", null, "breed", "Rex", "img.png", "user-2", "handler-1", "user-1", "ES", "team-1", null, null, null, 0L, 0L, null);
        givenDogs(dog);

        List<DogDTO> result = serviceCase.getDogs("user-1", true, new GetDogListCommand(false, true, null, null, null)).items();

        assertThat(result.getFirst().owned()).isFalse();
    }

    @Test
    void maps_dog_fields_to_dto_correctly() {
        Dog dog = new Dog("id-1", "ident-1", null, "breed", "Rex", "img.png", "user-1", "handler-1", "creator-1", "ES", "team-1", null, null, null, 0L, 0L, null);
        givenDogs(dog);

        DogDTO dto = serviceCase.getDogs("user-1", false, GetDogListCommand.ALL).items().getFirst();

        assertThat(dto.identification()).isEqualTo("id-1");
        assertThat(dto.name()).isEqualTo("Rex");
        assertThat(dto.image()).isEqualTo("img.png");
        assertThat(dto.owner()).isEqualTo("user-1");
        assertThat(dto.origin()).isEqualTo("ident-1");
        assertThat(dto.country()).isEqualTo("ES");
        assertThat(dto.team()).isEqualTo("team-1");
    }

    private Dog dog(String identification, String owner, String creator) {
        return new Dog(identification, "ident", null, "breed", "Rex", null, owner, "handler", creator, "ES", "team",
                null, null, null, 0L, 0L, null);
    }
}
