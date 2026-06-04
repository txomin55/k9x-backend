package com.k9x.infrastructure.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;

public class ClassificationCacheManagerAdapter implements ClassificationCacheManagerPort {

    private final Cache<String, Entry> cache;

    public ClassificationCacheManagerAdapter() {
        this.cache = Caffeine.newBuilder().maximumSize(1_000).build();
    }

    @Override
    public FetchObdxClassificationDTO getIfPresentAndValid(String eventId, int ttlSeconds) {
        Entry entry = cache.getIfPresent(eventId);
        if (entry == null) return null;
        long ageMillis = System.currentTimeMillis() - entry.computedAt();
        return ageMillis < (long) ttlSeconds * 1000L ? entry.dto() : null;
    }

    @Override
    public void put(String eventId, FetchObdxClassificationDTO dto) {
        cache.put(eventId, new Entry(dto, System.currentTimeMillis()));
    }

    private record Entry(FetchObdxClassificationDTO dto, long computedAt) {
    }
}
