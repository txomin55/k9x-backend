package com.k9x.application.events.snapshot.use_case.dto;

/**
 * A finished event that has no classification snapshot yet, together with its stored discipline so the
 * snapshot generation can dispatch per discipline.
 */
public record PendingSnapshotEventDTO(String eventId, String discipline) {
}
