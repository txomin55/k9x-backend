package com.k9x.application.collections.use_case.dto;

import java.util.List;

public record FetchCollectionDTO(String eventId, String eventName, String stageName,
                                 String competitionName, String discipline, String status,
                                 List<FetchCollectionJudgeDTO> judges) {}
