package com.k9x.application.events.obdx.use_cases.command;

import java.math.BigDecimal;

public record UpdateObdxScoreCommand(String judgeId, String exerciseId, String dogId, BigDecimal score) {
}
