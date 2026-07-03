package com.k9x.application.events.obdx.use_case.command;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

import java.util.List;

public record UpdateObdxEventCommand(
        String name,
        String configurationId,
        Long enrollmentDeadline,
        ObdxAvgMethod scoreCalculation,
        List<CompetitorCommand> competitors,
        List<ExerciseCommand> exercises,
        List<JudgeCommand> judges
) {
    public record CompetitorCommand(String dogId, Integer order, boolean bih) {
    }

    public record ExerciseCommand(String exerciseId, Integer order, List<String> tags) {
    }

    public record JudgeCommand(String judgeId, String collectorEmail) {
    }
}
