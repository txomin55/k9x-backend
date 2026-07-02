package com.k9x.application.collections.use_case.dto;

public record FetchCollectionCompetitorDTO(String dogId, String dogName, String dogIdentity, String breed,
                                           String owner, String handler, String team, String country,
                                           Short position, Boolean verified, boolean notCompeting,
                                           String status, Boolean bih, boolean scoresAllowed) {}
