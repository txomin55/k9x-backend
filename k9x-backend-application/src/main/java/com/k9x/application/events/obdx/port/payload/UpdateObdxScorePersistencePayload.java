package com.k9x.application.events.obdx.port.payload;

import com.k9x.application.events.obdx.use_cases.command.UpdateObdxScoreCommand;
import com.k9x.application.utils.date.DateUtils;

import java.math.BigDecimal;

public record UpdateObdxScorePersistencePayload(String judgeId, String exerciseId, String dogId,
                                                BigDecimal score, long lastUpdate) {

    public static UpdateObdxScorePersistencePayload from(UpdateObdxScoreCommand command) {
        return new UpdateObdxScorePersistencePayload(
                command.judgeId(),
                command.exerciseId(),
                command.dogId(),
                command.score(),
                DateUtils.nowUtcMillis());
    }
}
