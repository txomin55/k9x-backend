package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitorAlreadyNotCompetingException extends DomainException {

    public CompetitorAlreadyNotCompetingException() {
        super(ErrorEnum.COMPETITOR_ALREADY_NOT_COMPETING);
    }
}
