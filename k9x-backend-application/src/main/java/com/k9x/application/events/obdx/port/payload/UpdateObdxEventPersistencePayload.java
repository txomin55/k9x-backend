package com.k9x.application.events.obdx.port.payload;

import com.k9x.application.events.obdx.use_cases.command.UpdateObdxEventCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;

import java.util.List;

public record UpdateObdxEventPersistencePayload(
        String name,
        String configurationId,
        ObdxAvgMethod scoreCalculation,
        List<CompetitorItem> competitors,
        List<ExerciseItem> exercises,
        List<JudgeItem> judges,
        long lastUpdate
) {
    public static UpdateObdxEventPersistencePayload from(UpdateObdxEventCommand command, ObdxAvgMethod scoreCalculation) {
        return new UpdateObdxEventPersistencePayload(
                command.name(),
                command.configurationId(),
                scoreCalculation,
                command.competitors().stream()
                        .map(c -> new CompetitorItem(c.dogId(), c.order().shortValue()))
                        .toList(),
                command.exercises().stream()
                        .map(e -> new ExerciseItem(e.exerciseId(), e.order().shortValue(),
                                e.tags() == null ? new String[0] : e.tags().toArray(String[]::new)))
                        .toList(),
                command.judges().stream()
                        .map(j -> new JudgeItem(j.judgeId(), j.collectorEmail()))
                        .toList(),
                DateUtils.nowUtcMillis()
        );
    }

    public record CompetitorItem(String dogId, short position) {
    }

    public record ExerciseItem(String exerciseId, short position, String[] tags) {
    }

    public record JudgeItem(String judgeId, String collectorId) {
    }
}
