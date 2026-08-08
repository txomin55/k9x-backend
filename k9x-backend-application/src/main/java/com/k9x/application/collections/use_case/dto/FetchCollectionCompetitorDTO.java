package com.k9x.application.collections.use_case.dto;

public record FetchCollectionCompetitorDTO(String dogIdentification, String dogName, String dogOrigin, String breed,
                                           String owner, String handler, String team, String country,
                                           Short startNumber, Short competitorNumber, Boolean verified,
                                           boolean notCompeting, String status, Boolean bih, String primer, Boolean reserve,
                                           boolean scoresAllowed) {}
