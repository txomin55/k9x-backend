package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxDuplicateDogException extends DomainException {

    public ObdxDuplicateDogException() {
        super(ErrorEnum.DUPLICATE_DOG_IN_EVENT);
    }
}
