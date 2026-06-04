package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxUserNotCollectorException extends DomainException {

    public ObdxUserNotCollectorException() {
        super(ErrorEnum.USER_NOT_COLLECTOR);
    }
}
