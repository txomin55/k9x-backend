package com.k9x.infrastructure.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;

import java.time.Duration;

public class EventClassificationCacheManagerAdapter implements EventClassificationCacheManagerPort {

    /**
     * The TTL every caller asks for today (30 s) is only checked on read, so without this an entry nobody reads
     * again stays in memory for good. The daily snapshot cron reads each event once: on 2026-09-24 it left 587
     * of them in the heap and died of OutOfMemoryError. Evicting on write time frees them; the read-side check
     * still enforces the caller's TTL, so a caller asking for longer only gets an earlier miss.
     */
    private static final Duration EVICT_AFTER = Duration.ofSeconds(30);
    private static final int MAX_ENTRIES = 200;

    private final Cache<String, Entry> cache;

    public EventClassificationCacheManagerAdapter() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(EVICT_AFTER)
                .maximumSize(MAX_ENTRIES)
                .build();
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
