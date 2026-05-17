package com.k9x.application.competitions.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitionAlreadyDeletedException extends DomainException {

    public CompetitionAlreadyDeletedException() {
        super(ErrorEnum.COMPETITION_ALREADY_DELETED);
    }
}
