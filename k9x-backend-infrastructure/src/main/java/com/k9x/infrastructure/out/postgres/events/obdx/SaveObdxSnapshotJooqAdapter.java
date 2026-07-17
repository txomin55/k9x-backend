package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.SaveObdxSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.Query;
import org.jooq.impl.DSL;

import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;

/**
 * Persists an OBDX event snapshot atomically: the per-competitor position and rank score, plus the snapshot
 * marker row, all inside one {@code dsl.transaction} so they commit or roll back together. The marker insert is
 * {@code ON CONFLICT DO NOTHING} so a concurrent run keeps the first snapshot; the competitor updates are
 * idempotent, so a retry after a failure simply re-stamps the same values.
 */
public class SaveObdxSnapshotJooqAdapter implements SaveObdxSnapshotPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public SaveObdxSnapshotJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String eventId, long snapshotAt, FetchClassificationDTO classification,
                     List<ObdxCompetitorPosition> competitors) {
        String json = serialize(eventId, classification);

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            List<? extends Query> batch = competitors.stream()
                    .map(c -> ctx.update(EVENT_COMPETITORS)
                            .set(EVENT_COMPETITORS.POSITION, c.position())
                            .set(EVENT_COMPETITORS.RANK_SCORE, c.rankScore())
                            .where(EVENT_COMPETITORS.EVENT_ID.eq(eventId)
                                    .and(EVENT_COMPETITORS.DOG_ID.eq(c.dogId()))))
                    .toList();
            if (!batch.isEmpty()) {
                ctx.batch(batch).execute();
            }

            EventSnapshot es = EventSnapshot.EVENT_SNAPSHOT;
            ctx.insertInto(es)
                    .set(es.EVENT_ID, eventId)
                    .set(es.TIMESTAMP, snapshotAt)
                    .set(es.SNAPSHOT, JSON.valueOf(json))
                    .onConflict(es.EVENT_ID)
                    .doNothing()
                    .execute();
        });
    }

    private String serialize(String eventId, FetchClassificationDTO snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize classification snapshot for event " + eventId, e);
        }
    }
}
