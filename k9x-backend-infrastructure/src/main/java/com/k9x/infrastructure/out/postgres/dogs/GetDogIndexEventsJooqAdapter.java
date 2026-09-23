package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogIndexEventsPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.ExtractionMetadata;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRank;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.SnapEventCompetitorsResults;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

import java.util.List;

/**
 * Reads the dog's {@code k9x.snap_dog_rank} rows — the exact input of the index cron — and decorates each with
 * its event, competition and final placing. No soft-delete filter on purpose: the cron counts every row, and a
 * chart that dropped one would not end on the index the directory shows. A started event can't be deleted
 * anyway, so a snapshotted one never is.
 */
public class GetDogIndexEventsJooqAdapter implements GetDogIndexEventsPersistencePort {

    private static final SnapDogRank RANKS = Tables.SNAP_DOG_RANK;
    private static final SnapEventCompetitorsResults RESULTS =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.SNAP_EVENT_COMPETITORS_RESULTS;
    private static final ExtractionMetadata EXTRACTIONS = Tables.EXTRACTION_METADATA;
    private static final String LATEST_EXTRACTION = "latest_extraction";

    private final DSLContext dsl;

    public GetDogIndexEventsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchDogIndexEventDTO> getIndexEvents(String dogIdentification) {
        Table<?> latestExtraction = latestExtraction();
        Field<Boolean> restricted = latestExtraction.field(EXTRACTIONS.RESTRICTED);

        return dsl.select(Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.STAGES.ID, RANKS.DISCIPLINE,
                        Tables.COMPETITIONS.COUNTRY, RANKS.APPLYING_TIMESTAMP, RANKS.RANK, RESULTS.POSITION,
                        RESULTS.TOTAL_SCORE, restricted)
                .from(RANKS)
                .join(Tables.EVENTS).on(Tables.EVENTS.ID.eq(RANKS.EVENT_ID))
                .join(Tables.STAGES).on(Tables.STAGES.ID.eq(Tables.EVENTS.STAGE_ID))
                .join(Tables.COMPETITIONS).on(Tables.COMPETITIONS.ID.eq(Tables.STAGES.COMPETITION_ID))
                .leftJoin(RESULTS).on(RESULTS.EVENT_ID.eq(RANKS.EVENT_ID)
                        .and(RESULTS.DOG_IDENTIFICATION.eq(RANKS.DOG_IDENTIFICATION)))
                .leftJoin(latestExtraction)
                .on(latestExtraction.field(EXTRACTIONS.COMPETITION_ID).eq(Tables.COMPETITIONS.ID))
                .where(RANKS.DOG_IDENTIFICATION.eq(dogIdentification))
                .fetch(r -> new FetchDogIndexEventDTO(
                        r.get(Tables.EVENTS.ID),
                        r.get(Tables.EVENTS.NAME),
                        r.get(Tables.STAGES.ID),
                        r.get(RANKS.DISCIPLINE),
                        r.get(Tables.COMPETITIONS.COUNTRY),
                        r.get(RANKS.APPLYING_TIMESTAMP),
                        r.get(RANKS.RANK),
                        r.get(RESULTS.POSITION),
                        r.get(RESULTS.TOTAL_SCORE),
                        Boolean.TRUE.equals(r.get(restricted))));
    }

    /** Only a competition's most recent extraction counts, as in {@link GetDogParticipationsJooqAdapter}. */
    private Table<?> latestExtraction() {
        return dsl.select(EXTRACTIONS.COMPETITION_ID, EXTRACTIONS.RESTRICTED)
                .distinctOn(EXTRACTIONS.COMPETITION_ID)
                .from(EXTRACTIONS)
                .orderBy(EXTRACTIONS.COMPETITION_ID, EXTRACTIONS.EXTRACTION_TIMESTAMP.desc())
                .asTable(LATEST_EXTRACTION);
    }
}
