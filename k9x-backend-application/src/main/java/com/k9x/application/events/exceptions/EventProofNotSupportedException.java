package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * The event's discipline prints no working booklet, so there is no proof document to return. A missing
 * document is a 404, not a bad request: the caller asked for a resource that does not exist for this event.
 */
public class EventProofNotSupportedException extends NotFoundResourceException {

    public EventProofNotSupportedException(String discipline) {
        super(ErrorEnum.EVENT_PROOF_NOT_SUPPORTED, new String[]{discipline == null ? "" : discipline});
    }
}
