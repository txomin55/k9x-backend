package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventCannotBeUpdatedException extends DomainException {

    public EventCannotBeUpdatedException() {
        super(ErrorEnum.EVENT_CANNOT_BE_UPDATED);
    }
}
