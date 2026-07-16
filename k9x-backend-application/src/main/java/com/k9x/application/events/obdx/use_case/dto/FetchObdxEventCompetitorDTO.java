package com.k9x.application.events.obdx.use_case.dto;

public record FetchObdxEventCompetitorDTO(String dogId, String dogName, String dogIdentity, String breed,
                                          String owner, String handler, String team, String country,
                                          Short startNumber, Short competitorNumber, Boolean verified, String status,
                                          Boolean bih, Boolean reserve) {
}
