package com.k9x.application.events.obdx.use_case.command;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;

import java.util.List;

public record UpdateObdxEventCommand(
        String name,
        String configurationId,
        Long enrollmentDeadline,
        ObdxAvgMethod scoreCalculation,
        List<CompetitorCommand> competitors,
        List<ExerciseCommand> exercises,
        List<JudgeCommand> judges,
        List<String> awards,
        String commissioner,
        ObdxEventCategory category
) {
    public record CompetitorCommand(String dogIdentification, Integer order, Integer competitorNumber, boolean bih, String primer, boolean reserve) {
    }

    public record ExerciseCommand(String exerciseId, Integer order, List<String> tags, List<String> judgeIds) {
    }

    public record JudgeCommand(String judgeId, String collectorEmail, boolean mainJudge) {
    }
}
