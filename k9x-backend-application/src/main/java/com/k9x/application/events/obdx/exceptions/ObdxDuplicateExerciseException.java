package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxDuplicateExerciseException extends DomainException {

    public ObdxDuplicateExerciseException() {
        super(ErrorEnum.DUPLICATE_EXERCISE_IN_EVENT);
    }
}
