package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogAlreadyEnrolledException extends DomainException {

    public DogAlreadyEnrolledException() {
        super(ErrorEnum.DUPLICATE_DOG_IN_EVENT);
    }
}
