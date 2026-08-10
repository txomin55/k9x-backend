package com.k9x.application.rankings.port;

import java.util.Collection;
import java.util.Set;

/**
 * Narrow lookup that tells which of the requested event ids exist and are not deleted.
 *
 * <p>Rankings declare their own port instead of reusing the competition ones on purpose: a ranking is
 * not part of the competition aggregate and only needs this one fact about events.
 */
public interface GetActiveEventIdsPersistencePort {

    Set<String> getActiveEventIds(Collection<String> eventIds);
}
