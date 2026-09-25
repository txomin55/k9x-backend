package com.k9x.application.judges.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class JudgeIdRequiredException extends DomainException {

    public JudgeIdRequiredException() {
        super(ErrorEnum.JUDGE_ID_REQUIRED);
    }
}
