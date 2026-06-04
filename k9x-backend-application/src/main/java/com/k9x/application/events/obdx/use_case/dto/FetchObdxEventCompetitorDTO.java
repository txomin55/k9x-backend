package com.k9x.application.events.obdx.use_case.dto;

public record FetchObdxEventCompetitorDTO(String dogId, String dogName, String dogIdentity, String breed,
                                          String owner, String team, String country,
                                          Short position, Boolean verified, String status) {
}
