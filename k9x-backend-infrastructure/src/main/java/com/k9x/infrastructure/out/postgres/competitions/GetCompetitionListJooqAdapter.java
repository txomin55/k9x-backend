package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.use_case.dto.FetchEventDTO;
import com.k9x.application.competitions.use_case.dto.FetchStageDTO;
import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetCompetitionListJooqAdapter implements GetCompetitionListPersistencePort {

    private final DSLContext dsl;

    public GetCompetitionListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCompetitionDTO> getCompetitions(String creator) {
        List<Record> records = dsl.select()
                .from(Tables.COMPETITIONS)
                .leftJoin(Tables.STAGES).on(Tables.STAGES.COMPETITION_ID.eq(Tables.COMPETITIONS.ID)
                        .and(Tables.STAGES.DELETED_AT.isNull()))
                .leftJoin(Tables.EVENTS).on(Tables.EVENTS.STAGE_ID.eq(Tables.STAGES.ID)
                        .and(Tables.EVENTS.DELETED_AT.isNull()))
                .where(Tables.COMPETITIONS.CREATOR.eq(creator))
                .and(Tables.COMPETITIONS.DELETED_AT.isNull())
                .fetch();

        Map<String, FetchCompetitionDTO> competitionMap = new LinkedHashMap<>();
        Map<String, List<FetchStageDTO>> stagesMap = new LinkedHashMap<>();
        Map<String, FetchStageDTO> stageById = new LinkedHashMap<>();

        for (Record r : records) {
            String competitionId = r.get(Tables.COMPETITIONS.ID);
            competitionMap.putIfAbsent(competitionId, new FetchCompetitionDTO(
                    competitionId,
                    r.get(Tables.COMPETITIONS.NAME),
                    r.get(Tables.COMPETITIONS.DESCRIPTION),
                    r.get(Tables.COMPETITIONS.COUNTRY),
                    r.get(Tables.COMPETITIONS.ADDRESS),
                    null,
                    null
            ));
            stagesMap.putIfAbsent(competitionId, new ArrayList<>());

            String stageId = r.get(Tables.STAGES.ID);
            if (stageId != null && !stageById.containsKey(stageId)) {
                FetchStageDTO stage = new FetchStageDTO(
                        stageId,
                        r.get(Tables.STAGES.NAME),
                        r.get(Tables.STAGES.DATE_FROM),
                        r.get(Tables.STAGES.DATE_TO),
                        new ArrayList<>()
                );
                stageById.put(stageId, stage);
                stagesMap.get(competitionId).add(stage);
            }

            String eventId = r.get(Tables.EVENTS.ID);
            if (eventId != null) {
                stageById.get(stageId).events().add(new FetchEventDTO(
                        eventId,
                        r.get(Tables.EVENTS.NAME),
                        r.get(Tables.EVENTS.DISCIPLINE)
                ));
            }
        }

        return competitionMap.entrySet().stream()
                .map(e -> {
                    FetchCompetitionDTO c = e.getValue();
                    return new FetchCompetitionDTO(c.id(), c.name(), c.description(), c.country(), c.address(), null, stagesMap.get(e.getKey()));
                })
                .toList();
    }
}
