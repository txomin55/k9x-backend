package com.k9x.application.events.use_cases.dto;

import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventJudgeDTO;

import java.util.List;

public record FetchEventDetailDTO(FetchObdxEventDTO obdx,
                                  List<FetchObdxEventCompetitorDTO> competitors,
                                  List<FetchEventExerciseDTO> exercises,
                                  List<FetchObdxEventJudgeDTO> judges,
                                  FetchEventConfigurationDTO configuration) {
}
