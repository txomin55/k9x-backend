package com.k9x.application.notifications.port;

import java.util.List;
import java.util.Set;

public interface GetEventRecipientsPersistencePort {

    /**
     * The distinct users to notify about the given events: the owners of the dogs currently on their
     * rosters, plus the users subscribed to any of those events. The roster half is derived on read rather
     * than kept in a subscription table, so a deleted dog or a transferred ownership can never leave a
     * stale recipient behind; the subscribed half is the user's explicit opt-in.
     */
    Set<String> getRecipientIds(List<String> eventIds);
}
