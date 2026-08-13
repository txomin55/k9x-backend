package com.k9x.domain.events.valueobjects;

public record EventJudge(
        String judgeId,
        String judgeName,
        String collectorEmail,
        boolean mainJudge
) {
}
