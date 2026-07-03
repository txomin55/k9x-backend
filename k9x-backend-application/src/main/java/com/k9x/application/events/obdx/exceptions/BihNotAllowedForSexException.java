package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class BihNotAllowedForSexException extends DomainException {

    public BihNotAllowedForSexException() {
        super(ErrorEnum.BIH_NOT_ALLOWED_FOR_SEX);
    }
}
