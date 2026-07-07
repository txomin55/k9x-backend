package com.k9x.application.events.use_case.dto;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;

import java.util.List;

public record FetchEventExerciseDTO(String id, String name, Integer position, List<String> tags,
                                    List<FetchObdxEventJudgeDTO> judges) {
}
