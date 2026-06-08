package com.k9x.domain.aggregates.events;

public record EventJudge(
        String judgeId,
        String judgeName,
        String collectorEmail
) {
}
