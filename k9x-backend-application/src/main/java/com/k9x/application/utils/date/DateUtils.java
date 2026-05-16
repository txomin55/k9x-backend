package com.k9x.application.utils.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtils {

    private DateUtils() {}

    public static long nowUtcMillis() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }
}
