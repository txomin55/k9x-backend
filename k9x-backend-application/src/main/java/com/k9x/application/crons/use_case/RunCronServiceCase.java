package com.k9x.application.crons.use_case;

import com.k9x.application.crons.use_case.command.CronJob;
import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

/**
 * Runs a scheduled job on demand, outside its schedule: the classification snapshots (daily, 01:00 UTC) or the
 * dog index history (02:00 UTC on the 1st and 16th). Only the support account may trigger it: both read a lot
 * at once, so it is an operation, not a feature.
 *
 * <p>Both are safe to repeat: the snapshots only cover events that do not have one yet, and the history only
 * appends what is newer than each dog's latest record.
 */
public class RunCronServiceCase {

    static final String SUPPORT_EMAIL = "k9x.support@gmail.com";

    private final GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase;
    private final GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase;

    public RunCronServiceCase(GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase,
                              GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase) {
        this.generateEventSnapshotsServiceCase = generateEventSnapshotsServiceCase;
        this.generateDogRankHistoryServiceCase = generateDogRankHistoryServiceCase;
    }

    /**
     * @return how many rows the job wrote: snapshots generated, or history records appended.
     */
    public int run(CronJob job, String email) {
        if (!SUPPORT_EMAIL.equalsIgnoreCase(email)) {
            throw new UnauthorizedResourceException();
        }
        return switch (job) {
            case SNAPSHOTS -> generateEventSnapshotsServiceCase.generateSnapshots();
            case DOG_RANK_HISTORY -> generateDogRankHistoryServiceCase.generateDogRankHistory();
        };
    }
}
