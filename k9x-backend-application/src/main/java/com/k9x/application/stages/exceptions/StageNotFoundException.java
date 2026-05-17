package com.k9x.application.stages.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class StageNotFoundException extends NotFoundResourceException {

    public StageNotFoundException() {
        super(ErrorEnum.STAGE_NOT_FOUND);
    }
}
