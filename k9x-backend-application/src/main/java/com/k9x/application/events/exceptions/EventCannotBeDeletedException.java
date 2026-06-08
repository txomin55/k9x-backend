package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventCannotBeDeletedException extends DomainException {

    public EventCannotBeDeletedException() {
        super(ErrorEnum.EVENT_CANNOT_BE_DELETED);
    }
}
