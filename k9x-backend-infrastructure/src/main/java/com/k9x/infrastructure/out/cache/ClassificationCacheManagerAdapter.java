package com.k9x.infrastructure.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;

import java.time.Duration;

public class ClassificationCacheManagerAdapter implements ClassificationCacheManagerPort {

    /**
     * The TTL every caller asks for today (30 s) is only checked on read, so without this an entry nobody reads
     * again stays in memory for good. The daily snapshot cron reads each event once: on 2026-09-24 it left 587
     * of them in the heap and died of OutOfMemoryError. Evicting on write time frees them; the read-side check
     * still enforces the caller's TTL, so a caller asking for longer only gets an earlier miss.
     */
    private static final Duration EVICT_AFTER = Duration.ofSeconds(30);
    private static final int MAX_ENTRIES = 200;

    private final Cache<String, Entry> cache;

    public ClassificationCacheManagerAdapter() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(EVICT_AFTER)
                .maximumSize(MAX_ENTRIES)
                .build();
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
