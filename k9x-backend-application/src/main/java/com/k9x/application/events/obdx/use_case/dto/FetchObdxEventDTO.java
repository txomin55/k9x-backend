package com.k9x.application.events.obdx.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;

import java.util.List;

/**
 * Read model of an OBDX event. Besides the event's own fields it carries the few identity bits of its stage
 * and competition — start date, competition name, organizing group and address — because the printable
 * working-booklet proof needs them and they are already hydrated in the competition aggregate. They are not
 * projected to the REST response of the event detail endpoint — {@code source} is the exception, since the
 * detail view warns the organizer when the data was collected outside k9x.
 */
public record FetchObdxEventDTO(String id, String name, String stageId, String stageName, String discipline,
                                String status, Long enrollmentDeadline, ObdxAvgMethod scoreCalculation,
                                List<String> awards, String commissioner, ObdxEventCategory category,
                                Long stageDateFrom,
                                String competitionName, String organizerName, String address,
                                CompetitionSource source) {

    public FetchObdxEventDTO {
        awards = awards == null ? List.of() : awards;
    }
}
