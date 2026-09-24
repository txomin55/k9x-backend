package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowEventDTO;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator;
import com.k9x.infrastructure.out.postgres.competitions.LatestExtractionQuery;
import com.k9x.infrastructure.out.postgres.events.EventProjectionFields;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Organizers;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventInfo;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query projection of one stage's public detail, read from the tables instead of the competition aggregate: the
 * stage with its competition, its events with whether they hold any score (aggregated in SQL), and their
 * competitors. Sibling stages and scores are never loaded, except to ask the domain whether every competitor of
 * an event is settled, which only matters while the stage has not finished by date.
 */
public class GetStageDetailJooqAdapter implements GetStageDetailPersistencePort {

    private static final Stages ST = Tables.STAGES;
    private static final Competitions CO = Tables.COMPETITIONS;
    private static final Organizers OR = Tables.ORGANIZERS;
    private static final Events EV = Tables.EVENTS;
    private static final Dogs DO = Tables.DOGS;
    private static final EventInfo EI = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO;
    private static final EventCompetitors EC =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;

    static final Field<String> ORGANIZER_NAME = OR.NAME.as("organizer_name");
    static final Field<String> DOG_HANDLER = DO.HANDLER.as("dog_handler");
    static final Field<String> DOG_TEAM = DO.TEAM.as("dog_team");
    static final Field<String> DOG_COUNTRY = DO.COUNTRY.as("dog_country");

    private final DSLContext dsl;
    private final CompetitionHydrator hydrator;

    public GetStageDetailJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public Optional<FetchStageDetailRowDTO> getStage(String stageId, long startOfTodayUtcMillis) {
        Record stage = dsl.select(ST.ID, ST.NAME, ST.DATE_FROM, ST.DATE_TO, ST.DELETED_AT, CO.ID, CO.NAME,
                        CO.ADDRESS, ORGANIZER_NAME)
                .from(ST)
                .join(CO).on(CO.ID.eq(ST.COMPETITION_ID))
                .leftJoin(OR).on(OR.USER_ID.eq(CO.CREATOR))
                .where(ST.ID.eq(stageId))
                .fetchOne();
        if (stage == null) {
            return Optional.empty();
        }

        var events = dsl.select(EV.ID, EV.NAME, EV.DISCIPLINE, EI.CONFIGURATION_ID, EV.DELETED_AT,
                        EV.ENROLLMENT_DEADLINE, EV.AWARDS, EV.RANK_SCORE, EventProjectionFields.HAS_ANY_SCORE)
                .from(EV)
                // LEFT JOIN on purpose: the OBDX settings row is written by the event update, so a just-created
                // event — or one of another discipline — legitimately has none.
                .leftJoin(EI).on(EI.EVENT_ID.eq(EV.ID))
                .where(EV.STAGE_ID.eq(stageId))
                .orderBy(EV.CREATED_AT.asc(), EV.ID.asc())
                .fetch();
        List<String> eventIds = events.stream().map(r -> r.get(EV.ID)).toList();
        Map<String, List<FetchStageDetailCompetitorDTO>> competitors = fetchCompetitors(eventIds);
        Set<String> settled = stage.get(ST.DATE_TO) >= startOfTodayUtcMillis
                ? fetchSettledEventIds(eventIds.stream().filter(competitors::containsKey).toList())
                : Set.of();

        return Optional.of(new FetchStageDetailRowDTO(stage.get(ST.ID), stage.get(ST.NAME), stage.get(ST.DATE_FROM),
                stage.get(ST.DATE_TO), stage.get(ST.DELETED_AT), stage.get(CO.NAME), stage.get(CO.ADDRESS),
                stage.get(ORGANIZER_NAME), LatestExtractionQuery.of(dsl, stage.get(CO.ID)),
                events.stream()
                        .map(r -> new FetchStageDetailRowEventDTO(r.get(EV.ID), r.get(EV.NAME), r.get(EV.DISCIPLINE),
                                r.get(EI.CONFIGURATION_ID), r.get(EV.DELETED_AT), r.get(EV.ENROLLMENT_DEADLINE),
                                r.get(EV.AWARDS) == null ? List.of() : Arrays.asList(r.get(EV.AWARDS)),
                                r.get(EV.RANK_SCORE), competitors.getOrDefault(r.get(EV.ID), List.of()),
                                Boolean.TRUE.equals(r.get(EventProjectionFields.HAS_ANY_SCORE)),
                                settled.contains(r.get(EV.ID))))
                        .toList()));
    }

    /**
     * Handler, team and country come from the event_competitors snapshot, frozen at inclusion; the dog's current
     * values are only the fallback for competitors included before snapshots existed.
     */
    private Map<String, List<FetchStageDetailCompetitorDTO>> fetchCompetitors(List<String> eventIds) {
        Map<String, List<FetchStageDetailCompetitorDTO>> result = new LinkedHashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        dsl.select(EC.EVENT_ID, EC.DOG_IDENTIFICATION, EC.VERIFIED, EC.HANDLER, EC.TEAM, EC.COUNTRY,
                        DO.NAME, DO.OWNER, DO.BREED, DOG_HANDLER, DOG_TEAM, DOG_COUNTRY)
                .from(EC)
                .leftJoin(DO).on(DO.IDENTIFICATION.eq(EC.DOG_IDENTIFICATION).and(DO.DELETED_AT.isNull()))
                .where(EC.EVENT_ID.in(eventIds))
                .orderBy(EC.START_NUMBER.asc().nullsLast(), EC.DOG_IDENTIFICATION.asc())
                .forEach(r -> result.computeIfAbsent(r.get(EC.EVENT_ID), _ -> new ArrayList<>())
                        .add(new FetchStageDetailCompetitorDTO(r.get(EC.DOG_IDENTIFICATION), r.get(DO.NAME),
                                r.get(DO.OWNER), firstNonBlank(r.get(EC.HANDLER), r.get(DOG_HANDLER)),
                                firstNonBlank(r.get(EC.COUNTRY), r.get(DOG_COUNTRY)),
                                firstNonBlank(r.get(EC.TEAM), r.get(DOG_TEAM)), r.get(DO.BREED),
                                Boolean.TRUE.equals(r.get(EC.VERIFIED)))));
        return result;
    }

    private static String firstNonBlank(String snapshot, String current) {
        return snapshot == null || snapshot.isBlank() ? current : snapshot;
    }

    private Set<String> fetchSettledEventIds(List<String> eventIdsWithCompetitors) {
        return hydrator.hydrateEvents(eventIdsWithCompetitors).stream()
                .filter(EventSnapshot::allCompetitorsSettled)
                .map(EventSnapshot::id)
                .collect(Collectors.toSet());
    }
}
