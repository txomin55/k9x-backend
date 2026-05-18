package com.k9x.application.stages.payload;

public record UpdateStagePersistencePayload(String name, Long dateFrom, Long dateTo, long lastUpdate) {
}
