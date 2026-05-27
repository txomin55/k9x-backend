package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class UserNotCollectorException extends DomainException {

    public UserNotCollectorException() {
        super(ErrorEnum.USER_NOT_COLLECTOR);
    }
}
