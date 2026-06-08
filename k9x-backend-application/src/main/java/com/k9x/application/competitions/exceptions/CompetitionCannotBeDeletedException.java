package com.k9x.application.competitions.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitionCannotBeDeletedException extends DomainException {

    public CompetitionCannotBeDeletedException() {
        super(ErrorEnum.COMPETITION_CANNOT_BE_DELETED);
    }
}
