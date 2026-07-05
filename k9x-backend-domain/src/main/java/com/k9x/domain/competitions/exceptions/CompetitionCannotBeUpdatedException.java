package com.k9x.domain.competitions.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitionCannotBeUpdatedException extends DomainException {

    public CompetitionCannotBeUpdatedException() {
        super(ErrorEnum.COMPETITION_CANNOT_BE_UPDATED);
    }
}
