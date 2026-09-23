package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetDogParticipationsPersistencePort;
import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.ExtractionMetadata;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.SnapEventCompetitorsResults;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

import java.util.List;

/**
 * A flat projection rather than a hydration of the root aggregate: the dog's history can span many competitions,
 * and hydrating each whole tree (every competitor's score matrix) to read one row per event would cost far more
 * than this single query on a small box. The results it reads are the snapshot's, which the aggregate does not
 * carry anyway.
 */
public class GetDogParticipationsJooqAdapter implements GetDogParticipationsPersistencePort {

    private static final EventCompetitors COMPETITORS =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
    private static final SnapEventCompetitorsResults RESULTS =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.SNAP_EVENT_COMPETITORS_RESULTS;
    private static final ExtractionMetadata EXTRACTIONS = Tables.EXTRACTION_METADATA;
    private static final String LATEST_EXTRACTION = "latest_extraction";

    private final DSLContext dsl;

    public GetDogParticipationsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<DogParticipationDTO> getParticipations(String dogIdentification) {
        Table<?> latestExtraction = latestExtraction();
        Field<Boolean> restricted = latestExtraction.field(EXTRACTIONS.RESTRICTED);

        return dsl.select(Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.STAGES.ID, Tables.STAGES.DATE_FROM,
                        Tables.COMPETITIONS.COUNTRY, RESULTS.POSITION, RESULTS.TOTAL_SCORE, RESULTS.RANK_SCORE,
                        restricted)
                .from(COMPETITORS)
                .join(Tables.EVENTS).on(Tables.EVENTS.ID.eq(COMPETITORS.EVENT_ID))
                .join(Tables.STAGES).on(Tables.STAGES.ID.eq(Tables.EVENTS.STAGE_ID))
                .join(Tables.COMPETITIONS).on(Tables.COMPETITIONS.ID.eq(Tables.STAGES.COMPETITION_ID))
                .leftJoin(RESULTS).on(RESULTS.EVENT_ID.eq(COMPETITORS.EVENT_ID)
                        .and(RESULTS.DOG_IDENTIFICATION.eq(COMPETITORS.DOG_IDENTIFICATION)))
                .leftJoin(latestExtraction)
                .on(latestExtraction.field(EXTRACTIONS.COMPETITION_ID).eq(Tables.COMPETITIONS.ID))
                .where(COMPETITORS.DOG_IDENTIFICATION.eq(dogIdentification))
                .and(Tables.EVENTS.DELETED_AT.isNull())
                .and(Tables.STAGES.DELETED_AT.isNull())
                .and(Tables.COMPETITIONS.DELETED_AT.isNull())
                .fetch(r -> new DogParticipationDTO(
                        r.get(Tables.EVENTS.ID),
                        r.get(Tables.EVENTS.NAME),
                        r.get(Tables.STAGES.ID),
                        r.get(Tables.STAGES.DATE_FROM),
                        r.get(Tables.COMPETITIONS.COUNTRY),
                        r.get(RESULTS.POSITION),
                        r.get(RESULTS.TOTAL_SCORE),
                        r.get(RESULTS.RANK_SCORE),
                        Boolean.TRUE.equals(r.get(restricted))));
    }

    /**
     * Only a competition's most recent extraction describes the data currently loaded — the same rule the
     * competition hydrator follows — so that is the one whose {@code restricted} flag counts. Competitions created
     * through the app have no row and are never restricted.
     */
    private Table<?> latestExtraction() {
        return dsl.select(EXTRACTIONS.COMPETITION_ID, EXTRACTIONS.RESTRICTED)
                .distinctOn(EXTRACTIONS.COMPETITION_ID)
                .from(EXTRACTIONS)
                .orderBy(EXTRACTIONS.COMPETITION_ID, EXTRACTIONS.EXTRACTION_TIMESTAMP.desc())
                .asTable(LATEST_EXTRACTION);
    }
}
