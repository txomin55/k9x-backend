package com.k9x.application.judges.port.payload;

import com.k9x.application.judges.use_case.command.UpdateJudgeCommand;
import com.k9x.application.utils.date.DateUtils;

public record UpdateJudgePersistencePayload(String name, long lastUpdate) {

    public static UpdateJudgePersistencePayload from(UpdateJudgeCommand command) {
        return new UpdateJudgePersistencePayload(command.name(), DateUtils.nowUtcMillis());
    }
}
