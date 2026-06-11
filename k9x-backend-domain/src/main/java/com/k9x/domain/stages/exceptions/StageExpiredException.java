package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageExpiredException extends DomainException {

    public StageExpiredException() {
        super(ErrorEnum.STAGE_EXPIRED);
    }
}
