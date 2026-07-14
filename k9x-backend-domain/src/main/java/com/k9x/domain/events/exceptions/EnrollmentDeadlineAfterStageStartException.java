package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class EnrollmentDeadlineAfterStageStartException extends DomainException {

    public EnrollmentDeadlineAfterStageStartException() {
        super(ErrorEnum.ENROLLMENT_DEADLINE_AFTER_STAGE_START);
    }
}
