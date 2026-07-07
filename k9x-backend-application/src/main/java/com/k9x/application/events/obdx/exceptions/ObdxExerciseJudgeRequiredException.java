package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxExerciseJudgeRequiredException extends DomainException {

    public ObdxExerciseJudgeRequiredException(String exerciseId) {
        super(ErrorEnum.EXERCISE_JUDGE_REQUIRED, new String[]{exerciseId});
    }
}
