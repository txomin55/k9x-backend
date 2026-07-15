package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageDateToBeforeDateFromException extends DomainException {

    public StageDateToBeforeDateFromException() {
        super(ErrorEnum.STAGE_DATE_TO_BEFORE_DATE_FROM);
    }
}
