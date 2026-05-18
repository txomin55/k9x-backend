package com.k9x.application.stages.port.payload;

import com.k9x.application.stages.use_case.command.UpdateStageCommand;
import com.k9x.application.utils.date.DateUtils;

public record UpdateStagePersistencePayload(String name, Long dateFrom, Long dateTo, long lastUpdate) {

    public static UpdateStagePersistencePayload from(UpdateStageCommand command) {
        return new UpdateStagePersistencePayload(command.name(), command.dateFrom(), command.dateTo(), DateUtils.nowUtcMillis());
    }
}
