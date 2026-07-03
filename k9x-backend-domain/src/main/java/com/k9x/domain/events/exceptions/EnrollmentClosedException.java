package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EnrollmentClosedException extends DomainException {

    public EnrollmentClosedException() {
        super(ErrorEnum.ENROLLMENT_CLOSED);
    }
}
