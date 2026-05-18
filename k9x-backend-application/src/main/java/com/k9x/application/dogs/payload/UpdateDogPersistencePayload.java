package com.k9x.application.dogs.payload;

public record UpdateDogPersistencePayload(String name, String image, String breed, String identity,
                                          String owner, String team, String country, long lastUpdate) {
}
