package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.events.aggregates.*;
import com.k9x.domain.events.valueobjects.*;
import com.k9x.domain.events.status.*;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Organizers;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Users;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventExercises;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventScores;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.*;

/**
 * Hydrates the full {@link CompetitionSnapshot} root aggregate (stages → events → competitors / exercises /
 * judges / scores) with a handful of queries stitched in memory. Soft-deleted children are kept so the
 * aggregate is faithful; read-models filter them as needed and lifecycle status accounts for them.
 */
public class CompetitionHydrator {

    private final DSLContext dsl;

    public CompetitionHydrator(DSLContext dsl) {
        this.dsl = dsl;
    }

    private static Sex toSex(String stored) {
        if (stored == null) {
            return null;
        }
        try {
            return Sex.valueOf(stored.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<String> toList(Iterable<String> values) {
        List<String> list = new ArrayList<>();
        values.forEach(list::add);
        return list;
    }

    public List<CompetitionSnapshot> hydrate(Condition competitionCondition) {
        Map<String, CompetitionShell> competitions = fetchCompetitions(competitionCondition);
        if (competitions.isEmpty()) {
            return List.of();
        }

        Map<String, List<StageSnapshot>> stagesByCompetition = new LinkedHashMap<>();
        Map<String, List<EventSnapshot>> eventsByStage = fetchEvents(competitions.keySet());

        fetchStages(competitions.keySet()).forEach(shell -> {
            StageSnapshot stage = new StageSnapshot(shell.id, shell.name, shell.competitionId, shell.creator,
                    shell.dateFrom, shell.dateTo, shell.lastUpdate, shell.createdAt, shell.deletedAt,
                    eventsByStage.getOrDefault(shell.id, new ArrayList<>()));
            stagesByCompetition.computeIfAbsent(shell.competitionId, _ -> new ArrayList<>()).add(stage);
        });

        return competitions.values().stream()
                .map(c -> new CompetitionSnapshot(c.id, c.name, c.creator, c.organizerName, c.country,
                        c.description, c.address, c.coordAlt, c.coordLong, c.lastUpdate, c.createdAt,
                        c.deletedAt, stagesByCompetition.getOrDefault(c.id, new ArrayList<>())))
                .toList();
    }

    private Map<String, CompetitionShell> fetchCompetitions(Condition condition) {
        var co = Tables.COMPETITIONS;
        Organizers o = Tables.ORGANIZERS;
        Field<String> organizerName = o.NAME.as("organizer_name");

        Map<String, CompetitionShell> result = new LinkedHashMap<>();
        dsl.select(co.ID, co.NAME, co.CREATOR, organizerName, co.COUNTRY, co.DESCRIPTION, co.ADDRESS,
                        co.COORD_ALT, co.COORD_LONG, co.LAST_UPDATE, co.CREATED_AT, co.DELETED_AT)
                .from(co)
                .leftJoin(o).on(o.USER_ID.eq(co.CREATOR))
                .where(condition)
                .fetch()
                .forEach(r -> {
                    CompetitionShell shell = new CompetitionShell();
                    shell.id = r.get(co.ID);
                    shell.name = r.get(co.NAME);
                    shell.creator = r.get(co.CREATOR);
                    shell.organizerName = r.get(organizerName);
                    shell.country = r.get(co.COUNTRY);
                    shell.description = r.get(co.DESCRIPTION);
                    shell.address = r.get(co.ADDRESS);
                    shell.coordAlt = r.get(co.COORD_ALT);
                    shell.coordLong = r.get(co.COORD_LONG);
                    shell.lastUpdate = r.get(co.LAST_UPDATE);
                    shell.createdAt = r.get(co.CREATED_AT);
                    shell.deletedAt = r.get(co.DELETED_AT);
                    result.put(shell.id, shell);
                });
        return result;
    }

    private List<StageShell> fetchStages(Iterable<String> competitionIds) {
        var st = Tables.STAGES;
        return dsl.select(st.ID, st.NAME, st.COMPETITION_ID, st.CREATOR, st.DATE_FROM, st.DATE_TO,
                        st.LAST_UPDATE, st.CREATED_AT, st.DELETED_AT)
                .from(st)
                .where(st.COMPETITION_ID.in(toList(competitionIds)))
                .orderBy(st.DATE_FROM.asc())
                .fetch(r -> {
                    StageShell shell = new StageShell();
                    shell.id = r.get(st.ID);
                    shell.name = r.get(st.NAME);
                    shell.competitionId = r.get(st.COMPETITION_ID);
                    shell.creator = r.get(st.CREATOR);
                    shell.dateFrom = r.get(st.DATE_FROM);
                    shell.dateTo = r.get(st.DATE_TO);
                    shell.lastUpdate = r.get(st.LAST_UPDATE);
                    shell.createdAt = r.get(st.CREATED_AT);
                    shell.deletedAt = r.get(st.DELETED_AT);
                    return shell;
                });
    }

    private Map<String, List<EventSnapshot>> fetchEvents(Iterable<String> competitionIds) {
        var ev = Tables.EVENTS;
        var st = Tables.STAGES;

        List<EventShell> eventShells = dsl.select(ev.ID, ev.CONFIGURATION_ID, ev.DISCIPLINE, ev.NAME,
                        ev.STAGE_ID, ev.CREATOR, ev.ENROLLMENT_DEADLINE, ev.LAST_UPDATE, ev.CREATED_AT,
                        ev.DELETED_AT, ev.SCORE_CALCULATION, ev.AWARDS, ev.RANK_SCORE, ev.INTERNATIONAL)
                .from(ev)
                .join(st).on(st.ID.eq(ev.STAGE_ID))
                .where(st.COMPETITION_ID.in(toList(competitionIds)))
                .orderBy(ev.CREATED_AT.asc())
                .fetch(r -> {
                    EventShell shell = new EventShell();
                    shell.id = r.get(ev.ID);
                    shell.configurationId = r.get(ev.CONFIGURATION_ID);
                    shell.discipline = r.get(ev.DISCIPLINE);
                    shell.name = r.get(ev.NAME);
                    shell.stageId = r.get(ev.STAGE_ID);
                    shell.creator = r.get(ev.CREATOR);
                    shell.enrollmentDeadline = r.get(ev.ENROLLMENT_DEADLINE);
                    shell.lastUpdate = r.get(ev.LAST_UPDATE);
                    shell.createdAt = r.get(ev.CREATED_AT);
                    shell.deletedAt = r.get(ev.DELETED_AT);
                    shell.scoreCalculation = r.get(ev.SCORE_CALCULATION);
                    shell.awards = r.get(ev.AWARDS);
                    shell.rankScore = r.get(ev.RANK_SCORE);
                    shell.international = r.get(ev.INTERNATIONAL);
                    return shell;
                });

        List<String> eventIds = eventShells.stream().map(s -> s.id).toList();
        Map<String, List<EventCompetitor>> competitors = fetchCompetitors(eventIds);
        Map<String, List<EventExercise>> exercises = fetchExercises(eventIds);
        Map<String, List<EventJudge>> judges = fetchJudges(eventIds);
        Map<String, List<Score>> scores = fetchScores(eventIds);

        Map<String, List<EventSnapshot>> eventsByStage = new LinkedHashMap<>();
        for (EventShell s : eventShells) {
            EventSnapshot event = new EventSnapshot(s.id, s.configurationId, s.discipline, s.name, s.stageId, s.creator,
                    s.enrollmentDeadline, s.lastUpdate, s.createdAt, s.deletedAt,
                    s.scoreCalculation == null ? null : ObdxAvgMethod.valueOf(s.scoreCalculation),
                    competitors.getOrDefault(s.id, new ArrayList<>()),
                    exercises.getOrDefault(s.id, new ArrayList<>()),
                    judges.getOrDefault(s.id, new ArrayList<>()),
                    scores.getOrDefault(s.id, new ArrayList<>()),
                    s.awards == null ? List.of() : Arrays.asList(s.awards),
                    s.rankScore,
                    s.international);
            eventsByStage.computeIfAbsent(s.stageId, _ -> new ArrayList<>()).add(event);
        }
        return eventsByStage;
    }

    private Map<String, List<EventCompetitor>> fetchCompetitors(List<String> eventIds) {
        Map<String, List<EventCompetitor>> result = new LinkedHashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = Tables.DOGS;
        dsl.select(ec.EVENT_ID, ec.DOG_IDENTIFICATION, ec.START_NUMBER, ec.COMPETITOR_NUMBER, ec.VERIFIED, ec.NOT_COMPETING, ec.BIH, ec.PRIMER,
                        ec.RESERVE,
                        d.NAME, d.OWNER, d.HANDLER, d.TEAM, d.COUNTRY, d.BREED, d.ORIGIN, d.LICENSE, d.SEX,
                        d.THREE_FCI_GENERATIONS_CONFIRMED)
                .from(ec)
                .leftJoin(d).on(d.IDENTIFICATION.eq(ec.DOG_IDENTIFICATION).and(d.DELETED_AT.isNull()))
                .where(ec.EVENT_ID.in(eventIds))
                .orderBy(ec.START_NUMBER.asc().nullsLast(), ec.DOG_IDENTIFICATION.asc())
                .forEach(r -> result.computeIfAbsent(r.get(ec.EVENT_ID), _ -> new ArrayList<>())
                        .add(new EventCompetitor(
                                r.get(ec.DOG_IDENTIFICATION), r.get(d.NAME), r.get(d.OWNER), r.get(d.HANDLER), r.get(d.TEAM),
                                r.get(d.COUNTRY), r.get(d.BREED), r.get(d.ORIGIN), r.get(d.LICENSE), toSex(r.get(d.SEX)),
                                r.get(ec.START_NUMBER), r.get(ec.COMPETITOR_NUMBER), r.get(ec.VERIFIED),
                                Boolean.TRUE.equals(r.get(ec.NOT_COMPETING)), r.get(ec.BIH), r.get(ec.PRIMER),
                                r.get(ec.RESERVE),
                                r.get(d.THREE_FCI_GENERATIONS_CONFIRMED))));
        return result;
    }

    private Map<String, List<EventExercise>> fetchExercises(List<String> eventIds) {
        Map<String, List<EventExercise>> result = new LinkedHashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        EventExercises ee = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_EXERCISES;
        dsl.select(ee.EVENT_ID, ee.EXERCISE_ID, ee.POSITION, ee.TAGS, ee.JUDGES)
                .from(ee)
                .where(ee.EVENT_ID.in(eventIds))
                .orderBy(ee.POSITION.asc())
                .forEach(r -> result.computeIfAbsent(r.get(ee.EVENT_ID), _ -> new ArrayList<>())
                        .add(new EventExercise(
                                r.get(ee.EXERCISE_ID), r.get(ee.POSITION),
                                r.get(ee.TAGS) == null ? List.of() : Arrays.stream(r.get(ee.TAGS)).toList(),
                                r.get(ee.JUDGES) == null ? List.of() : Arrays.stream(r.get(ee.JUDGES)).toList())));
        return result;
    }

    private Map<String, List<EventJudge>> fetchJudges(List<String> eventIds) {
        Map<String, List<EventJudge>> result = new LinkedHashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        Judges j = Tables.JUDGES;
        Users u = Tables.USERS;
        dsl.select(ej.EVENT_ID, ej.JUDGE_ID, j.NAME, u.EMAIL)
                .from(ej)
                .join(j).on(j.ID.eq(ej.JUDGE_ID).and(j.DELETED_AT.isNull()))
                .leftJoin(u).on(u.ID.eq(ej.COLLECTOR_ID))
                .where(ej.EVENT_ID.in(eventIds))
                .forEach(r -> result.computeIfAbsent(r.get(ej.EVENT_ID), _ -> new ArrayList<>())
                        .add(new EventJudge(r.get(ej.JUDGE_ID), r.get(j.NAME), r.get(u.EMAIL))));
        return result;
    }

    private Map<String, List<Score>> fetchScores(List<String> eventIds) {
        Map<String, List<Score>> result = new LinkedHashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        EventScores es = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_SCORES;
        dsl.select(es.EVENT_ID, es.EXERCISE_ID, es.JUDGE_ID, es.DOG_IDENTIFICATION, es.SCORE, es.LAST_UPDATE,
                        es.YELLOW_CARD, es.RED_CARD)
                .from(es)
                .where(es.EVENT_ID.in(eventIds))
                .forEach(r -> result.computeIfAbsent(r.get(es.EVENT_ID), _ -> new ArrayList<>())
                        .add(new Score(r.get(es.EXERCISE_ID), r.get(es.JUDGE_ID), r.get(es.DOG_IDENTIFICATION),
                                r.get(es.SCORE), r.get(es.LAST_UPDATE),
                                r.get(es.YELLOW_CARD), r.get(es.RED_CARD))));
        return result;
    }

    private static final class CompetitionShell {
        String id, name, creator, organizerName, country, description, address;
        Double coordAlt, coordLong;
        long lastUpdate, createdAt;
        Long deletedAt;
    }

    private static final class StageShell {
        String id, name, competitionId, creator;
        long dateFrom, dateTo, lastUpdate, createdAt;
        Long deletedAt;
    }

    private static final class EventShell {
        String id, configurationId, discipline, name, stageId, creator, scoreCalculation;
        Integer rankScore;
        Boolean international;
        Long enrollmentDeadline;
        long lastUpdate, createdAt;
        Long deletedAt;
        String[] awards;
    }
}
