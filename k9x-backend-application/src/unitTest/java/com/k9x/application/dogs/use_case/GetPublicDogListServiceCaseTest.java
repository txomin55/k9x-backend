package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.GetPublicDogListPersistencePort;
import com.k9x.application.dogs.port.payload.PublicDogListFilter;
import com.k9x.application.dogs.port.payload.PublicDogListPage;
import com.k9x.application.dogs.use_case.command.GetPublicDogListCommand;
import com.k9x.application.dogs.use_case.dto.FetchPublicDogDTO;
import com.k9x.application.dogs.use_case.dto.PublicDogListDTO;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.dogs.rank.DogRankIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicDogListServiceCaseTest {

    @Mock
    private GetPublicDogListPersistencePort getPublicDogListPersistencePort;

    @Captor
    private ArgumentCaptor<PublicDogListFilter> filterCaptor;

    private GetPublicDogListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetPublicDogListServiceCase(getPublicDogListPersistencePort);
    }

    private void givenDogs(FetchPublicDogDTO... dogs) {
        when(getPublicDogListPersistencePort.getDogs(any()))
                .thenReturn(new PublicDogListPage(List.of(dogs), dogs.length));
    }

    private PublicDogListFilter capturedFilter() {
        verify(getPublicDogListPersistencePort).getDogs(filterCaptor.capture());
        return filterCaptor.getValue();
    }

    private FetchPublicDogDTO dog(Integer rank) {
        return new FetchPublicDogDTO("981098106001010", "Rex", "Ana", "ES", Sex.MALE, "BORDER_COLLIE", rank);
    }

    @Test
    void maps_every_presentation_field_of_the_dog() {
        givenDogs(dog(742));

        PublicDogListDTO dogs = serviceCase.getDogs(GetPublicDogListCommand.ALL);

        assertThat(dogs.items()).hasSize(1);
        assertThat(dogs.items().getFirst().identification()).isEqualTo("981098106001010");
        assertThat(dogs.items().getFirst().name()).isEqualTo("Rex");
        assertThat(dogs.items().getFirst().handler()).isEqualTo("Ana");
        assertThat(dogs.items().getFirst().country()).isEqualTo("ES");
        assertThat(dogs.items().getFirst().sex()).isEqualTo(Sex.MALE);
        assertThat(dogs.items().getFirst().breed()).isEqualTo("BORDER_COLLIE");
        assertThat(dogs.items().getFirst().rank()).isEqualTo("742");
    }

    @Test
    void says_the_index_is_not_generated_when_the_dog_has_none() {
        givenDogs(dog(null));

        PublicDogListDTO dogs = serviceCase.getDogs(GetPublicDogListCommand.ALL);

        assertThat(dogs.items().getFirst().rank()).isEqualTo(DogRankIndex.NOT_GENERATED);
    }

    @Test
    void passes_the_searches_and_the_country_down_to_persistence() {
        givenDogs();

        serviceCase.getDogs(new GetPublicDogListCommand("re", "an", "ES", null, null));

        PublicDogListFilter filter = capturedFilter();
        assertThat(filter.nameContains()).isEqualTo("re");
        assertThat(filter.handlerContains()).isEqualTo("an");
        assertThat(filter.country()).isEqualTo("ES");
    }

    @Test
    void drops_blank_searches_so_they_do_not_filter_anything() {
        givenDogs();

        serviceCase.getDogs(new GetPublicDogListCommand("  ", "", "  ", null, null));

        PublicDogListFilter filter = capturedFilter();
        assertThat(filter.nameContains()).isNull();
        assertThat(filter.handlerContains()).isNull();
        assertThat(filter.country()).isNull();
    }

    @Test
    void translates_the_requested_page_into_an_offset_and_a_limit() {
        givenDogs();

        serviceCase.getDogs(new GetPublicDogListCommand(null, null, null, 2, 20));

        PublicDogListFilter filter = capturedFilter();
        assertThat(filter.offset()).isEqualTo(40);
        assertThat(filter.limit()).isEqualTo(20);
        assertThat(filter.paginated()).isTrue();
    }

    @Test
    void returns_the_whole_list_as_a_single_page_when_no_size_is_requested() {
        givenDogs(dog(742), dog(null));

        PublicDogListDTO dogs = serviceCase.getDogs(GetPublicDogListCommand.ALL);

        assertThat(capturedFilter().paginated()).isFalse();
        assertThat(dogs.page()).isZero();
        assertThat(dogs.size()).isEqualTo(2);
        assertThat(dogs.total()).isEqualTo(2);
        assertThat(dogs.totalPages()).isEqualTo(1);
    }

    @Test
    void reports_the_page_window_and_the_whole_match_count_when_paginated() {
        when(getPublicDogListPersistencePort.getDogs(any()))
                .thenReturn(new PublicDogListPage(List.of(dog(742)), 137));

        PublicDogListDTO dogs = serviceCase.getDogs(new GetPublicDogListCommand(null, null, null, 1, 20));

        assertThat(dogs.page()).isEqualTo(1);
        assertThat(dogs.size()).isEqualTo(20);
        assertThat(dogs.total()).isEqualTo(137);
        assertThat(dogs.totalPages()).isEqualTo(7);
    }
}
