package com.k9x.application.dogs.rank.port.payload;

import com.k9x.application.utils.date.DateUtils;

import java.util.Map;

/**
 * One {@code k9x.snap_dog_index_history} record: the dog's index in a discipline, effective at
 * {@code applyingTimestamp} (the event's snapshot instant for event records, the evaluation instant for
 * degradations), stamped with the persistence-time {@code timestamp}, plus free-form string metadata
 * (persisted as a JSON string, like the notifications metadata) describing what produced it — an event result
 * ({@code type=EVENT} + {@code eventId}) or inactivity degradation ({@code type=TIME_DEGRADATION} +
 * {@code month}).
 */
public record DogRankHistoryPayload(String dogIdentification, String discipline, int rank, long timestamp,
                                    long applyingTimestamp, Map<String, String> metadata) {

    public static final String TYPE_KEY = "type";
    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_TIME_DEGRADATION = "TIME_DEGRADATION";

    public static DogRankHistoryPayload fromEvent(String dogIdentification, String discipline, int rank,
                                                  long applyingTimestamp, String eventId) {
        return new DogRankHistoryPayload(dogIdentification, discipline, rank, DateUtils.nowUtcMillis(), applyingTimestamp,
                Map.of(TYPE_KEY, TYPE_EVENT, "eventId", eventId));
    }

    public static DogRankHistoryPayload fromTimeDegradation(String dogIdentification, String discipline, int rank,
                                                            long applyingTimestamp, int month) {
        return new DogRankHistoryPayload(dogIdentification, discipline, rank, DateUtils.nowUtcMillis(), applyingTimestamp,
                Map.of(TYPE_KEY, TYPE_TIME_DEGRADATION, "month", String.valueOf(month)));
    }
}
