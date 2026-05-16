package com.k9x.application.judges.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class JudgeAlreadyDeletedException extends DomainException {

    public JudgeAlreadyDeletedException() {
        super(ErrorEnum.JUDGE_ALREADY_DELETED);
    }
}
