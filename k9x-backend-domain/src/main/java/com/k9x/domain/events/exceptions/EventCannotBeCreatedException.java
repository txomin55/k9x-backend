package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventCannotBeCreatedException extends DomainException {

    public EventCannotBeCreatedException() {
        super(ErrorEnum.EVENT_CANNOT_BE_CREATED);
    }
}
