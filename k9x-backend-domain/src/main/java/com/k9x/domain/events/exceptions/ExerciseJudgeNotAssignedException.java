package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ExerciseJudgeNotAssignedException extends DomainException {

    public ExerciseJudgeNotAssignedException(String judgeId, String exerciseId) {
        super(ErrorEnum.EXERCISE_JUDGE_NOT_ASSIGNED, new String[]{judgeId, exerciseId});
    }
}
