package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxScoreNotAllowedException extends DomainException {

    public ObdxScoreNotAllowedException() {
        super(ErrorEnum.SCORE_NOT_ALLOWED);
    }
}
