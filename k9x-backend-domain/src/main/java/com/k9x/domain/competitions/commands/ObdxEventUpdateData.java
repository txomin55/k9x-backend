package com.k9x.domain.competitions.commands;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

import java.util.List;

public record ObdxEventUpdateData(String name, String configurationId, ObdxAvgMethod scoreCalculation,
                                  Long enrollmentDeadline, List<ObdxCompetitorItem> competitors,
                                  List<ObdxExerciseItem> exercises, List<ObdxJudgeItem> judges) {
}
