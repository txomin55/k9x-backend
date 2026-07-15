package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetObdxEventSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.util.Optional;

public class GetObdxEventSnapshotJooqAdapter implements GetObdxEventSnapshotPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public GetObdxEventSnapshotJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<FetchClassificationDTO> getSnapshot(String eventId) {
        EventSnapshot es = EventSnapshot.EVENT_SNAPSHOT;
        JSON json = dsl.select(es.SNAPSHOT)
                .from(es)
                .where(es.EVENT_ID.eq(eventId))
                .fetchOne(es.SNAPSHOT);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json.data(), FetchClassificationDTO.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize classification snapshot for event " + eventId, e);
        }
    }
}
