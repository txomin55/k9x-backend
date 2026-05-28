package com.k9x.application.collections.use_case;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCollectionListServiceCaseTest {

    @Mock
    private GetCollectionListPersistencePort getCollectionListPersistencePort;

    private GetCollectionListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetCollectionListServiceCase(getCollectionListPersistencePort);
    }

    @Test
    void returns_collections_for_collector() {
        List<FetchCollectionDTO> rawCollections = List.of(
                new FetchCollectionDTO("event-1", "Event A", "Stage A", "Competition A", null,
                        List.of(new FetchCollectionJudgeDTO("judge-1", "Judge One")))
        );
        when(getCollectionListPersistencePort.getCollections(eq("collector@test.com"), anyLong()))
                .thenReturn(rawCollections);

        List<FetchCollectionDTO> result = serviceCase.getCollections("collector@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().eventId()).isEqualTo("event-1");
        assertThat(result.getFirst().status()).isEqualTo("OPEN");
        verify(getCollectionListPersistencePort).getCollections(eq("collector@test.com"), anyLong());
    }

    @Test
    void sets_status_open_on_all_results() {
        List<FetchCollectionDTO> rawCollections = List.of(
                new FetchCollectionDTO("event-1", "Event A", "Stage A", "Competition A", null, List.of()),
                new FetchCollectionDTO("event-2", "Event B", "Stage B", "Competition B", null, List.of())
        );
        when(getCollectionListPersistencePort.getCollections(eq("collector@test.com"), anyLong()))
                .thenReturn(rawCollections);

        List<FetchCollectionDTO> result = serviceCase.getCollections("collector@test.com");

        assertThat(result).allSatisfy(c -> assertThat(c.status()).isEqualTo("OPEN"));
    }

    @Test
    void returns_empty_list_when_no_collections_found() {
        when(getCollectionListPersistencePort.getCollections(eq("collector@test.com"), anyLong()))
                .thenReturn(List.of());

        List<FetchCollectionDTO> result = serviceCase.getCollections("collector@test.com");

        assertThat(result).isEmpty();
    }
}
