package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxExerciseJudgeNotFoundException extends NotFoundResourceException {

    public ObdxExerciseJudgeNotFoundException(String judgeId) {
        super(ErrorEnum.EXERCISE_JUDGE_NOT_FOUND, new String[]{judgeId});
    }
}
