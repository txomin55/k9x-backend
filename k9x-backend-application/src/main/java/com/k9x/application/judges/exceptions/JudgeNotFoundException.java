package com.k9x.application.judges.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class JudgeNotFoundException extends NotFoundResourceException {

    public JudgeNotFoundException() {
        super(ErrorEnum.JUDGE_NOT_FOUND);
    }
}
