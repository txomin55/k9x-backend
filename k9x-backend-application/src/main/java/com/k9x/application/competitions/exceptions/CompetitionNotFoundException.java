package com.k9x.application.competitions.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitionNotFoundException extends NotFoundResourceException {

    public CompetitionNotFoundException() {
        super(ErrorEnum.COMPETITION_NOT_FOUND);
    }
}
