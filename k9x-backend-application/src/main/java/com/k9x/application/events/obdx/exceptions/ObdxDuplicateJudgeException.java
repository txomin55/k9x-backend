package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxDuplicateJudgeException extends DomainException {

    public ObdxDuplicateJudgeException() {
        super(ErrorEnum.DUPLICATE_JUDGE_IN_EVENT);
    }
}
