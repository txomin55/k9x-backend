package com.k9x.application.dogs.rank.use_case.dto;

import java.math.BigDecimal;

public record FetchDogRankDTO(String dogId, BigDecimal rank, long timestamp) {
}
