package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.GetSelectableCompetitionsPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchSelectableCompetitionDTO;
import com.k9x.application.competitions.use_case.dto.FetchSelectableStageDTO;
import com.k9x.application.shared.IdNameDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One flat query for the whole competition → trial → event tree, ids and names only.
 *
 * <p>The joined tables are filtered in the ON clause, not the WHERE, so a competition with no trials and a
 * trial with no events still come back — an empty branch is legitimate here, the picker just shows nothing
 * under it.
 */
public class GetSelectableCompetitionsJooqAdapter implements GetSelectableCompetitionsPersistencePort {

    private final DSLContext dsl;

    public GetSelectableCompetitionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchSelectableCompetitionDTO> getSelectableCompetitions() {
        record Branch(String name, Map<String, FetchSelectableStageDTO> stages) {}

        Map<String, Branch> byCompetition = new LinkedHashMap<>();
        Map<String, List<IdNameDTO>> eventsByStage = new LinkedHashMap<>();

        dsl.select(
                        Tables.COMPETITIONS.ID,
                        Tables.COMPETITIONS.NAME,
                        Tables.STAGES.ID,
                        Tables.STAGES.NAME,
                        Tables.EVENTS.ID,
                        Tables.EVENTS.NAME)
                .from(Tables.COMPETITIONS)
                .leftJoin(Tables.STAGES)
                .on(Tables.STAGES.COMPETITION_ID.eq(Tables.COMPETITIONS.ID)
                        .and(Tables.STAGES.DELETED_AT.isNull()))
                .leftJoin(Tables.EVENTS)
                .on(Tables.EVENTS.STAGE_ID.eq(Tables.STAGES.ID)
                        .and(Tables.EVENTS.DELETED_AT.isNull()))
                .where(Tables.COMPETITIONS.DELETED_AT.isNull())
                .orderBy(Tables.COMPETITIONS.NAME.asc(), Tables.STAGES.DATE_FROM.asc(),
                        Tables.EVENTS.NAME.asc())
                .fetch()
                .forEach(row -> {
                    String competitionId = row.get(Tables.COMPETITIONS.ID);
                    Branch branch = byCompetition.computeIfAbsent(competitionId,
                            ignored -> new Branch(row.get(Tables.COMPETITIONS.NAME), new LinkedHashMap<>()));

                    String stageId = row.get(Tables.STAGES.ID);
                    if (stageId == null) {
                        return;
                    }
                    List<IdNameDTO> events =
                            eventsByStage.computeIfAbsent(stageId, ignored -> new ArrayList<>());
                    branch.stages().computeIfAbsent(stageId,
                            ignored -> new FetchSelectableStageDTO(stageId, row.get(Tables.STAGES.NAME), events));

                    String eventId = row.get(Tables.EVENTS.ID);
                    if (eventId != null) {
                        events.add(new IdNameDTO(eventId, row.get(Tables.EVENTS.NAME)));
                    }
                });

        return byCompetition.entrySet().stream()
                .map(entry -> new FetchSelectableCompetitionDTO(
                        entry.getKey(),
                        entry.getValue().name(),
                        List.copyOf(entry.getValue().stages().values())))
                .toList();
    }
}
