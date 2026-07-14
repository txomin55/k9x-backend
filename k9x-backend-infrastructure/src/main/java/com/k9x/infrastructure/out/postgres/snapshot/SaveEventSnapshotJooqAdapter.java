package com.k9x.infrastructure.out.postgres.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.SaveEventSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
import org.jooq.DSLContext;
import org.jooq.JSON;

public class SaveEventSnapshotJooqAdapter implements SaveEventSnapshotPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public SaveEventSnapshotJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String eventId, long timestamp, FetchClassificationDTO snapshot) {
        String json;
        try {
            json = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize classification snapshot for event " + eventId, e);
        }

        EventSnapshot es = EventSnapshot.EVENT_SNAPSHOT;
        // ON CONFLICT DO NOTHING: another instance may have already stored this event's snapshot; keep the first.
        dsl.insertInto(es)
                .set(es.EVENT_ID, eventId)
                .set(es.TIMESTAMP, timestamp)
                .set(es.SNAPSHOT, JSON.valueOf(json))
                .onConflict(es.EVENT_ID)
                .doNothing()
                .execute();
    }
}
