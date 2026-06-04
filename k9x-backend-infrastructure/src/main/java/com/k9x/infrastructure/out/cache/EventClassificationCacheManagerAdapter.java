package com.k9x.infrastructure.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;

public class EventClassificationCacheManagerAdapter implements EventClassificationCacheManagerPort {

    private final Cache<String, Entry> cache;

    public EventClassificationCacheManagerAdapter() {
        this.cache = Caffeine.newBuilder().maximumSize(1_000).build();
    }

    @Override
    public EventClassificationContextDTO getIfPresentAndValid(String eventId, int ttlSeconds) {
        Entry entry = cache.getIfPresent(eventId);
        if (entry == null) return null;
        long ageMillis = System.currentTimeMillis() - entry.computedAt();
        return ageMillis < (long) ttlSeconds * 1000L ? entry.context() : null;
    }

    @Override
    public void put(String eventId, EventClassificationContextDTO context) {
        cache.put(eventId, new Entry(context, System.currentTimeMillis()));
    }

    private record Entry(EventClassificationContextDTO context, long computedAt) {
    }
}
