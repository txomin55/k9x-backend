package com.k9x.domain.commons.exception;

import com.k9x.domain.commons.exception.error.ErrorEnum;

public class UnauthorizedResourceException extends DomainException {

    public UnauthorizedResourceException() {
        super(ErrorEnum.UNAUTHORIZED_RESOURCE_ERROR);
    }
}
