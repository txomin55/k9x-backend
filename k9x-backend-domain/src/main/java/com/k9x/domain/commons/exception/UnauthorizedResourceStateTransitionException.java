package com.k9x.domain.commons.exception;

import com.k9x.domain.commons.exception.error.ErrorEnum;

public class UnauthorizedResourceStateTransitionException extends DomainException {

    public UnauthorizedResourceStateTransitionException() {
        super(ErrorEnum.UNAUTHORIZED_RESOURCE_STATE_TRANSITION_ERROR);
    }
}
