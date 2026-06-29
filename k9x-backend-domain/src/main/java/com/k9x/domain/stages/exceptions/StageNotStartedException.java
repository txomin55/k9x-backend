package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageNotStartedException extends DomainException {

    public StageNotStartedException() {
        super(ErrorEnum.STAGE_NOT_STARTED);
    }
}
