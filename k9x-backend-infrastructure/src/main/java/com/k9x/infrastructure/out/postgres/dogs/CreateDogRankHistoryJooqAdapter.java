package com.k9x.infrastructure.out.postgres.dogs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankHistoryPayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogIndexHistory;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;

import java.util.List;

/**
 * Appends {@code k9x.snap_dog_index_history} records; the metadata map is serialized to a JSON string,
 * mirroring the notifications metadata column. Inserts do nothing on conflict (same dog and applying
 * timestamp), so a retried run re-stamps nothing.
 */
public class CreateDogRankHistoryJooqAdapter implements CreateDogRankHistoryPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public CreateDogRankHistoryJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    /**
     * One transaction per call: its use case is a batch that commits block by block (it is deliberately not a
     * {@code TransactionalUseCase}), so a block is appended whole or not at all.
     */
    @Override
    public void create(List<DogRankHistoryPayload> records) {
        if (records.isEmpty()) {
            return;
        }
        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);
            SnapDogIndexHistory h = Tables.SNAP_DOG_INDEX_HISTORY;
            List<? extends Query> batch = records.stream()
                    .map(r -> ctx.insertInto(h)
                            .set(h.DOG_IDENTIFICATION, r.dogIdentification())
                            .set(h.RANK, r.rank())
                            .set(h.TIMESTAMP, r.timestamp())
                            .set(h.APPLYING_TIMESTAMP, r.applyingTimestamp())
                            .set(h.METADATA, serialize(r))
                            .onConflict(h.DOG_IDENTIFICATION, h.APPLYING_TIMESTAMP)
                            .doNothing())
                    .toList();
            ctx.batch(batch).execute();
        });
    }

    private String serialize(DogRankHistoryPayload record) {
        try {
            return objectMapper.writeValueAsString(record.metadata());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize dog rank history metadata for " + record.dogIdentification(), e);
        }
    }
}
