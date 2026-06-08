package com.k9x.application.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageCannotBeDeletedException extends DomainException {

    public StageCannotBeDeletedException() {
        super(ErrorEnum.STAGE_CANNOT_BE_DELETED);
    }
}
