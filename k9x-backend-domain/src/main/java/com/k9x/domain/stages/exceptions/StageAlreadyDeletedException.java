package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageAlreadyDeletedException extends DomainException {

    public StageAlreadyDeletedException() {
        super(ErrorEnum.STAGE_ALREADY_DELETED);
    }
}
