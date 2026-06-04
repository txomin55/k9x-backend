package com.k9x.application.events.use_case.dto;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;

import java.util.List;

public record FetchEventDetailDTO(FetchObdxEventDTO obdx,
                                  List<FetchObdxEventCompetitorDTO> competitors,
                                  List<FetchEventExerciseDTO> exercises,
                                  List<FetchObdxEventJudgeDTO> judges,
                                  FetchEventConfigurationDTO configuration) {
}
