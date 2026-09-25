package com.k9x.application.judges.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class JudgeNameRequiredException extends DomainException {

    public JudgeNameRequiredException() {
        super(ErrorEnum.JUDGE_NAME_REQUIRED);
    }
}
