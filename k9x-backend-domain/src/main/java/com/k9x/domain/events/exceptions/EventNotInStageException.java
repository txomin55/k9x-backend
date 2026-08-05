package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventNotInStageException extends DomainException {

    public EventNotInStageException() {
        super(ErrorEnum.EVENT_NOT_IN_STAGE);
    }
}
