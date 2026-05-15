package com.k9x.domain.exceptions;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class UnauthorizedResourceException extends DomainException {

    public UnauthorizedResourceException() {
        super(ErrorEnum.UNAUTHORIZED_RESOURCE_ERROR);
    }
}
