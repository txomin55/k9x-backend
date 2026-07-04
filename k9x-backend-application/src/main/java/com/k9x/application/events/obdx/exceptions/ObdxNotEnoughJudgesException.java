package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxNotEnoughJudgesException extends DomainException {

    public ObdxNotEnoughJudgesException() {
        super(ErrorEnum.NOT_ENOUGH_JUDGES_FOR_MID_AVG);
    }
}
