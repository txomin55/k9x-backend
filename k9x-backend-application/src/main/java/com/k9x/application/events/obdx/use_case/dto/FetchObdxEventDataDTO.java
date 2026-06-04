package com.k9x.application.events.obdx.use_case.dto;

import java.util.List;

public record FetchObdxEventDataDTO(List<FetchObdxEventCompetitorDTO> competitors,
                                    List<FetchObdxEventExerciseDTO> exercises,
                                    List<FetchObdxEventJudgeDTO> judges) {
}
