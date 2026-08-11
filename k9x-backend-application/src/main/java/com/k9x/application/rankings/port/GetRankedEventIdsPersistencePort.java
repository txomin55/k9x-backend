package com.k9x.application.rankings.port;

import java.util.Set;

public interface GetRankedEventIdsPersistencePort {

    /** Every event id that appears in some ranking. */
    Set<String> getRankedEventIds();
}
