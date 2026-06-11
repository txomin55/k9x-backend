package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventAlreadyDeletedException extends DomainException {

    public EventAlreadyDeletedException() {
        super(ErrorEnum.EVENT_ALREADY_DELETED);
    }
}
