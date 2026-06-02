package com.k9x.application.events.obdx.port.payload;

import com.k9x.application.events.obdx.use_cases.command.EnrollObdxEventCommand;
import com.k9x.application.utils.date.DateUtils;

public record EnrollObdxEventPersistencePayload(String dogId, long lastUpdate) {

    public static EnrollObdxEventPersistencePayload from(EnrollObdxEventCommand command) {
        return new EnrollObdxEventPersistencePayload(command.dogId(), DateUtils.nowUtcMillis());
    }
}
