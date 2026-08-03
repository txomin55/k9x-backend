package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.SaveObdxSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.SnapEventClassification;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.SnapEventCompetitorsResults;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.Query;
import org.jooq.impl.DSL;

import java.util.List;

/**
 * Persists an OBDX event snapshot atomically: the per-competitor results
 * ({@code obdx.snap_event_competitors_results}: position, total score and rank score), the dog's OBDX rank
 * history row ({@code k9x.snap_dog_rank}), the event-level granted-awards list, plus the snapshot marker row
 * ({@code obdx.snap_event_classification}), all inside one {@code dsl.transaction} so they commit or roll back
 * together. Every insert is {@code ON CONFLICT DO NOTHING} and the granted-awards update is idempotent, so a
 * concurrent run keeps the first snapshot and a retry after a failure simply re-stamps the same values.
 */
public class SaveObdxSnapshotJooqAdapter implements SaveObdxSnapshotPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public SaveObdxSnapshotJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String eventId, long snapshotAt, FetchObdxClassificationDTO obdx,
                     List<ObdxCompetitorPosition> competitors, List<String> grantedAwards) {
        String json = serialize(eventId, obdx);

        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);

            SnapEventCompetitorsResults results = SnapEventCompetitorsResults.SNAP_EVENT_COMPETITORS_RESULTS;
            List<? extends Query> batch = competitors.stream()
                    .map(c -> ctx.insertInto(results)
                            .set(results.EVENT_ID, eventId)
                            .set(results.DOG_ID, c.dogId())
                            .set(results.POSITION, c.position())
                            .set(results.TOTAL_SCORE, c.totalScore())
                            .set(results.RANK_SCORE, c.rankScore())
                            .onConflict(results.EVENT_ID, results.DOG_ID)
                            .doNothing())
                    .toList();
            if (!batch.isEmpty()) {
                ctx.batch(batch).execute();
            }

            List<? extends Query> rankHistory = competitors.stream()
                    .filter(c -> c.rankScore() != null)
                    .map(c -> ctx.insertInto(Tables.SNAP_DOG_RANK)
                            .set(Tables.SNAP_DOG_RANK.DOG_ID, c.dogId())
                            .set(Tables.SNAP_DOG_RANK.DISCIPLINE, Discipline.OBDX.name())
                            .set(Tables.SNAP_DOG_RANK.RANK, c.rankScore())
                            .set(Tables.SNAP_DOG_RANK.TIMESTAMP, snapshotAt)
                            .onConflict(Tables.SNAP_DOG_RANK.DOG_ID, Tables.SNAP_DOG_RANK.DISCIPLINE,
                                    Tables.SNAP_DOG_RANK.TIMESTAMP)
                            .doNothing())
                    .toList();
            if (!rankHistory.isEmpty()) {
                ctx.batch(rankHistory).execute();
            }

            ctx.update(Tables.EVENTS)
                    .set(Tables.EVENTS.GRANTED_AWARDS, grantedAwards.toArray(String[]::new))
                    .where(Tables.EVENTS.ID.eq(eventId))
                    .execute();

            SnapEventClassification es = SnapEventClassification.SNAP_EVENT_CLASSIFICATION;
            ctx.insertInto(es)
                    .set(es.EVENT_ID, eventId)
                    .set(es.TIMESTAMP, snapshotAt)
                    .set(es.SNAPSHOT, JSON.valueOf(json))
                    .onConflict(es.EVENT_ID)
                    .doNothing()
                    .execute();
        });
    }

    private String serialize(String eventId, FetchObdxClassificationDTO snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize classification snapshot for event " + eventId, e);
        }
    }
}
