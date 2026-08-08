package com.k9x.domain.competitions.commands;

import java.math.BigDecimal;

public record ScoreUpdated(String eventId, String judgeId, String exerciseId, String dogIdentification, BigDecimal score,
                           long lastUpdate) implements CompetitionChange {
}
