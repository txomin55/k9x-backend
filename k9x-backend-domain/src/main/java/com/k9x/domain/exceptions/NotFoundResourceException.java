package com.k9x.domain.exceptions;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class NotFoundResourceException extends DomainException {

    public NotFoundResourceException(ErrorEnum error) {
        super(error);
    }

    public NotFoundResourceException(ErrorEnum error, String[] args) {
        super(error, args);
    }
}
