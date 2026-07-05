package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageCannotBeUpdatedException extends DomainException {

    public StageCannotBeUpdatedException() {
        super(ErrorEnum.STAGE_CANNOT_BE_UPDATED);
    }
}
