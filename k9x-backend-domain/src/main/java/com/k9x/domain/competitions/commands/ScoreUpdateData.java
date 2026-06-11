package com.k9x.domain.competitions.commands;

import java.math.BigDecimal;

public record ScoreUpdateData(String judgeId, String exerciseId, String dogId, BigDecimal score) {
}
