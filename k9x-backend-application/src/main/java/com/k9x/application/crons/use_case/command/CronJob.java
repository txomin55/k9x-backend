package com.k9x.application.crons.use_case.command;

import java.util.Arrays;
import java.util.Optional;

/**
 * The scheduled jobs that can also be run by hand, each with the name the admin endpoint takes.
 */
public enum CronJob {
    SNAPSHOTS("snapshots"),
    DOG_RANK_HISTORY("dog-rank-history");

    private final String param;

    CronJob(String param) {
        this.param = param;
    }

    public String param() {
        return param;
    }

    public static Optional<CronJob> fromParam(String param) {
        return Arrays.stream(values()).filter(job -> job.param.equals(param)).findFirst();
    }
}
