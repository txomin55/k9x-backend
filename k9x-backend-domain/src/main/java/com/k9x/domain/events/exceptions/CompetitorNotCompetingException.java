package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitorNotCompetingException extends DomainException {

    public CompetitorNotCompetingException() {
        super(ErrorEnum.COMPETITOR_NOT_COMPETING);
    }
}
