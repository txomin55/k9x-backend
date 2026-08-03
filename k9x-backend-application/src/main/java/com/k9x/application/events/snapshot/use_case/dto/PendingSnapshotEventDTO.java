package com.k9x.application.events.snapshot.use_case.dto;

/**
 * A finished event that has no classification snapshot yet: its stored discipline (so the snapshot generation
 * can dispatch per discipline) and its stage's end instant — the {@code applying_timestamp} every snap row of
 * this event is stamped with, so a historical event ingested late still lands on its real date.
 */
public record PendingSnapshotEventDTO(String eventId, String discipline, long stageEndAt) {
}
