package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * Raised when an operation that only makes sense on a live event is attempted on a finished one, e.g.
 * announcing something to its competitors or subscribing to its notifications.
 */
public class EventFinishedException extends DomainException {

    public EventFinishedException() {
        super(ErrorEnum.EVENT_FINISHED);
    }
}
