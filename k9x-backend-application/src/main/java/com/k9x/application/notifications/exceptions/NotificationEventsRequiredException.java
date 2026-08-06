package com.k9x.application.notifications.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * Raised when an announcement carries no event to address: with no events there is nobody to notify, and
 * none of the event-level rules (active, in the stage, created by the user, not finished) would be
 * checked either.
 */
public class NotificationEventsRequiredException extends DomainException {

    public NotificationEventsRequiredException() {
        super(ErrorEnum.NOTIFICATION_EVENTS_REQUIRED);
    }
}
