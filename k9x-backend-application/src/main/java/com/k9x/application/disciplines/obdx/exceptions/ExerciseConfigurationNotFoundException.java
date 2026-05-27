package com.k9x.application.disciplines.obdx.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ExerciseConfigurationNotFoundException extends NotFoundResourceException {

    public ExerciseConfigurationNotFoundException() {
        super(ErrorEnum.EXERCISE_CONFIGURATION_NOT_FOUND);
    }
}
