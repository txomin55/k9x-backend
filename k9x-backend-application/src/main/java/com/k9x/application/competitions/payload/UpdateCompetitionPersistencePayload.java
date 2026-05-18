package com.k9x.application.competitions.payload;

public record UpdateCompetitionPersistencePayload(String name, String description, String country,
                                                  String address, Double coordAlt, Double coordLong,
                                                  long lastUpdate) {
}
