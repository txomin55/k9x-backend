package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowEventDTO;
import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator;
import com.k9x.infrastructure.out.postgres.events.EventProjectionFields;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.ExtractionMetadata;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Organizers;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query projection of the public stage list, read from the tables instead of the competition aggregate. The date
 * range is filtered in SQL, and each event's competitor count and whether it holds any score are aggregated in
 * SQL too, so no competitor or score row reaches the heap for the list itself.
 *
 * <p>The one fact SQL cannot give cheaply is whether every competitor is settled: it is the domain's
 * exercise × judge matrix. It only matters for an event whose stage has not finished by date and that has
 * competitors, so only those few events are hydrated, through {@link CompetitionHydrator#hydrateEvents}, and the
 * rule is asked of the domain.
 */
public class GetStagesJooqAdapter implements GetStageListPersistencePort {

    private static final Stages ST = Tables.STAGES;
    private static final Competitions CO = Tables.COMPETITIONS;
    private static final Organizers OR = Tables.ORGANIZERS;
    private static final Events EV = Tables.EVENTS;

    static final Field<String> ORGANIZER_NAME = OR.NAME.as("organizer_name");

    private final DSLContext dsl;
    private final CompetitionHydrator hydrator;

    public GetStagesJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public List<FetchStageListRowDTO> getStages(Long from, Long to, long startOfTodayUtcMillis) {
        List<StageRow> stages = fetchStages(from, to);
        if (stages.isEmpty()) {
            return List.of();
        }
        Map<String, CompetitionExtraction> extractions =
                fetchExtractions(stages.stream().map(StageRow::competitionId).distinct().toList());
        Map<String, Long> dateToByStage = stages.stream().collect(Collectors.toMap(StageRow::id, StageRow::dateTo));
        List<EventRow> events = fetchEvents(dateToByStage.keySet());
        Set<String> settledEventIds = fetchSettledEventIds(events.stream()
                .filter(e -> e.competitorCount() > 0 && dateToByStage.get(e.stageId()) >= startOfTodayUtcMillis)
                .map(EventRow::id)
                .toList());

        Map<String, List<FetchStageListRowEventDTO>> eventsByStage = new LinkedHashMap<>();
        events.forEach(e -> eventsByStage.computeIfAbsent(e.stageId(), _ -> new ArrayList<>())
                .add(new FetchStageListRowEventDTO(e.id(), e.name(), e.discipline(), e.deletedAt(),
                        e.enrollmentDeadline(), e.awards(), e.rankScore(), e.competitorCount(), e.hasAnyScore(),
                        settledEventIds.contains(e.id()))));

        return stages.stream()
                .map(s -> new FetchStageListRowDTO(s.id(), s.name(), s.dateFrom(), s.dateTo(), s.competitionName(),
                        s.country(), s.address(), s.coordAlt(), s.coordLong(), s.organizer(),
                        extractions.get(s.competitionId()), eventsByStage.getOrDefault(s.id(), List.of())))
                .toList();
    }

    private List<StageRow> fetchStages(Long from, Long to) {
        Condition condition = ST.DELETED_AT.isNull().and(CO.DELETED_AT.isNull());
        if (from != null) {
            condition = condition.and(ST.DATE_FROM.ge(from));
        }
        if (to != null) {
            condition = condition.and(ST.DATE_FROM.le(to));
        }
        return dsl.select(ST.ID, ST.NAME, ST.DATE_FROM, ST.DATE_TO, CO.ID, CO.NAME, CO.COUNTRY, CO.ADDRESS,
                        CO.COORD_ALT, CO.COORD_LONG, ORGANIZER_NAME)
                .from(ST)
                .join(CO).on(CO.ID.eq(ST.COMPETITION_ID))
                .leftJoin(OR).on(OR.USER_ID.eq(CO.CREATOR))
                .where(condition)
                .fetch(r -> new StageRow(r.get(ST.ID), r.get(ST.NAME), r.get(ST.DATE_FROM), r.get(ST.DATE_TO),
                        r.get(CO.ID), r.get(CO.NAME), r.get(CO.COUNTRY), r.get(CO.ADDRESS), r.get(CO.COORD_ALT),
                        r.get(CO.COORD_LONG), r.get(ORGANIZER_NAME)));
    }

    /**
     * A competition can be extracted more than once — a re-collection from a better source — so only the most
     * recent extraction is kept: it is the one that describes the data currently loaded.
     */
    private Map<String, CompetitionExtraction> fetchExtractions(List<String> competitionIds) {
        ExtractionMetadata em = Tables.EXTRACTION_METADATA;
        Map<String, CompetitionExtraction> result = new LinkedHashMap<>();
        dsl.select(em.COMPETITION_ID, em.EXTRACTION_ID, em.SOURCE, em.EXTRACTION_TIMESTAMP, em.TYPE,
                        em.RESTRICTED, em.CREATED_AT)
                .from(em)
                .where(em.COMPETITION_ID.in(competitionIds))
                .orderBy(em.EXTRACTION_TIMESTAMP.asc())
                .forEach(r -> result.put(r.get(em.COMPETITION_ID), new CompetitionExtraction(
                        r.get(em.EXTRACTION_ID), r.get(em.SOURCE), r.get(em.EXTRACTION_TIMESTAMP),
                        r.get(em.TYPE), Boolean.TRUE.equals(r.get(em.RESTRICTED)), r.get(em.CREATED_AT))));
        return result;
    }

    /** Every event of the stages, deleted ones included: the stage status accounts for them. */
    private List<EventRow> fetchEvents(Set<String> stageIds) {
        return dsl.select(EV.ID, EV.NAME, EV.DISCIPLINE, EV.STAGE_ID, EV.DELETED_AT, EV.ENROLLMENT_DEADLINE,
                        EV.AWARDS, EV.RANK_SCORE, EventProjectionFields.COMPETITOR_COUNT, EventProjectionFields.HAS_ANY_SCORE)
                .from(EV)
                .where(EV.STAGE_ID.in(stageIds))
                .orderBy(EV.CREATED_AT.asc(), EV.ID.asc())
                .fetch(r -> new EventRow(r.get(EV.ID), r.get(EV.NAME), r.get(EV.DISCIPLINE), r.get(EV.STAGE_ID),
                        r.get(EV.DELETED_AT), r.get(EV.ENROLLMENT_DEADLINE),
                        r.get(EV.AWARDS) == null ? List.of() : Arrays.asList(r.get(EV.AWARDS)),
                        r.get(EV.RANK_SCORE), r.get(EventProjectionFields.COMPETITOR_COUNT),
                        Boolean.TRUE.equals(r.get(EventProjectionFields.HAS_ANY_SCORE))));
    }

    private Set<String> fetchSettledEventIds(List<String> runningEventIds) {
        return hydrator.hydrateEvents(runningEventIds).stream()
                .filter(EventSnapshot::allCompetitorsSettled)
                .map(EventSnapshot::id)
                .collect(Collectors.toSet());
    }

    private record StageRow(String id, String name, long dateFrom, long dateTo, String competitionId,
                            String competitionName, String country, String address, Double coordAlt,
                            Double coordLong, String organizer) {
    }

    private record EventRow(String id, String name, String discipline, String stageId, Long deletedAt,
                            Long enrollmentDeadline, List<String> awards, Integer rankScore, int competitorCount,
                            boolean hasAnyScore) {
    }
}
