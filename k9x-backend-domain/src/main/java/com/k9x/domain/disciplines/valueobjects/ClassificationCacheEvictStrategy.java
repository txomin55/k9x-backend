package com.k9x.domain.disciplines.valueobjects;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

public enum ClassificationCacheEvictStrategy {
    OBDX(30, ObdxAvgMethod.MID_AVG);

    private final int ttlSeconds;
    private final ObdxAvgMethod avgMethod;

    ClassificationCacheEvictStrategy(int ttlSeconds, ObdxAvgMethod avgMethod) {
        this.ttlSeconds = ttlSeconds;
        this.avgMethod = avgMethod;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public ObdxAvgMethod getAvgMethod() {
        return avgMethod;
    }
}
