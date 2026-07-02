package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class YellowCardAlreadyRegisteredException extends DomainException {

    public YellowCardAlreadyRegisteredException() {
        super(ErrorEnum.YELLOW_CARD_ALREADY_REGISTERED);
    }
}
