package com.k9x.application.collections.use_case.dto;

public record FetchCollectionCompetitorDTO(String dogId, String dogName, String dogIdentity,
                                           String owner, String team, String country,
                                           Short position, Boolean verified, String status) {}
