package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class RedCardAlreadyRegisteredException extends DomainException {

    public RedCardAlreadyRegisteredException() {
        super(ErrorEnum.RED_CARD_ALREADY_REGISTERED);
    }
}
