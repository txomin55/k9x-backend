package com.k9x.infrastructure.out.postgres.extractions;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogStageDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.infrastructure.out.postgres.events.EventProjectionFields;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.ExtractionMetadata;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Query projection of the extraction log, read from the tables instead of the competition aggregate: two
 * queries, the stages with their competition and latest load instant, and their events with the competitor
 * count aggregated in SQL. No competitor or score row is loaded.
 */
public class GetExtractedCompetitionsJooqAdapter implements GetExtractedCompetitionsPersistencePort {

    private static final Stages ST = Tables.STAGES;
    private static final Competitions CO = Tables.COMPETITIONS;
    private static final Events EV = Tables.EVENTS;
    private static final ExtractionMetadata EM = Tables.EXTRACTION_METADATA;

    /**
     * A competition can be extracted more than once — a re-collection from a better source — so only its most
     * recent extraction counts: it is the one that describes the data currently loaded.
     */
    static final Table<?> LATEST_EXTRACTION = org.jooq.impl.DSL
            .select(EM.COMPETITION_ID, EM.CREATED_AT)
            .distinctOn(EM.COMPETITION_ID)
            .from(EM)
            .orderBy(EM.COMPETITION_ID, EM.EXTRACTION_TIMESTAMP.desc())
            .asTable("latest_extraction");
    static final Field<String> LATEST_COMPETITION_ID = LATEST_EXTRACTION.field(EM.COMPETITION_ID);
    static final Field<Long> LOADED_AT = LATEST_EXTRACTION.field(EM.CREATED_AT);

    private final DSLContext dsl;

    public GetExtractedCompetitionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchExtractionLogStageDTO> getExtractedStages() {
        List<StageRow> stages = dsl.select(ST.ID, ST.NAME, ST.DATE_FROM, ST.DATE_TO, CO.NAME, CO.COUNTRY, LOADED_AT)
                .from(ST)
                .join(CO).on(CO.ID.eq(ST.COMPETITION_ID))
                .join(LATEST_EXTRACTION).on(LATEST_COMPETITION_ID.eq(CO.ID))
                .where(CO.SOURCE.eq(CompetitionSource.EXTRACTION.name()))
                .and(CO.DELETED_AT.isNull())
                .and(ST.DELETED_AT.isNull())
                .and(LOADED_AT.isNotNull())
                .fetch(r -> new StageRow(r.get(ST.ID), r.get(ST.NAME), r.get(ST.DATE_FROM), r.get(ST.DATE_TO),
                        r.get(CO.NAME), r.get(CO.COUNTRY), r.get(LOADED_AT)));
        if (stages.isEmpty()) {
            return List.of();
        }

        Map<String, List<FetchExtractionLogEventDTO>> eventsByStage = new LinkedHashMap<>();
        dsl.select(EV.ID, EV.NAME, EV.DISCIPLINE, EV.STAGE_ID, EV.RANK_SCORE, EventProjectionFields.COMPETITOR_COUNT)
                .from(EV)
                .where(EV.STAGE_ID.in(stages.stream().map(StageRow::id).toList()))
                .and(EV.DELETED_AT.isNull())
                .orderBy(EV.CREATED_AT.asc(), EV.ID.asc())
                .forEach(r -> eventsByStage.computeIfAbsent(r.get(EV.STAGE_ID), _ -> new ArrayList<>())
                        .add(new FetchExtractionLogEventDTO(r.get(EV.ID), r.get(EV.NAME), r.get(EV.DISCIPLINE),
                                r.get(EventProjectionFields.COMPETITOR_COUNT), r.get(EV.RANK_SCORE))));

        return stages.stream()
                .map(s -> new FetchExtractionLogStageDTO(s.id(), s.name(), s.competitionName(), s.country(),
                        s.dateFrom(), s.dateTo(), s.loadedAt(), eventsByStage.getOrDefault(s.id(), List.of())))
                .toList();
    }

    private record StageRow(String id, String name, long dateFrom, long dateTo, String competitionName,
                            String country, long loadedAt) {
    }
}
