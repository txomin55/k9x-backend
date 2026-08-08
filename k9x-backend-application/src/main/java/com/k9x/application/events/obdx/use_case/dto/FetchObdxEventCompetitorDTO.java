package com.k9x.application.events.obdx.use_case.dto;

public record FetchObdxEventCompetitorDTO(String dogIdentification, String dogName, String dogOrigin, String breed,
                                          String owner, String handler, String team, String country,
                                          String sex, Short startNumber, Short competitorNumber, Boolean verified,
                                          String status, Boolean bih, Boolean reserve) {
}
