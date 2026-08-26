package com.k9x.domain.events.valueobjects;

import com.k9x.domain.dogs.aggregates.Dog;

/**
 * The dog's handler, country and team frozen at the moment the dog was included as a competitor of an
 * event — by enrollment or by the organizer's event update. The dog record keeps evolving (a handler
 * changes, a dog moves to another team or country), but the competitor list of an already-run event has
 * to keep showing who competed and for whom at that time, so the values are copied onto the competitor
 * row instead of being read live from the dog.
 * <p>
 * The snapshot is taken only on the <em>inclusion</em>: re-saving an event that already had the dog as a
 * competitor keeps the snapshot it was included with (see {@link #atInclusion}). Rows created before the
 * snapshot existed carry {@code null} fields.
 */
public record CompetitorDogSnapshot(String handler, String country, String team) {

    public static final CompetitorDogSnapshot EMPTY = new CompetitorDogSnapshot(null, null, null);

    public static CompetitorDogSnapshot of(Dog dog) {
        if (dog == null) {
            return EMPTY;
        }
        return new CompetitorDogSnapshot(dog.handler(), dog.country(), dog.team());
    }

    /**
     * Resolves the snapshot to persist for a competitor: the one it was already included with when the dog
     * is already a competitor of the event, the dog's current state when it is joining now. A competitor row
     * predating the snapshot carries nothing to preserve, so it is filled in with the current state too.
     */
    public static CompetitorDogSnapshot atInclusion(CompetitorDogSnapshot alreadyIncludedWith, CompetitorDogSnapshot current) {
        if (alreadyIncludedWith == null || alreadyIncludedWith.isEmpty()) {
            return current == null ? EMPTY : current;
        }
        return alreadyIncludedWith;
    }

    public boolean isEmpty() {
        return handler == null && country == null && team == null;
    }
}
