package com.k9x.domain.competitions.commands;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

import java.util.List;

public record ObdxEventInfoUpdated(String eventId, String name, String configurationId,
                                   ObdxAvgMethod scoreCalculation, Long enrollmentDeadline,
                                   List<ObdxCompetitorItem> competitors, List<ObdxExerciseItem> exercises,
                                   List<ObdxJudgeItem> judges, long lastUpdate) implements CompetitionChange {
}
