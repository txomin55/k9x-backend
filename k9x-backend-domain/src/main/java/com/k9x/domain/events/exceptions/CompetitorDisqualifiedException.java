package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitorDisqualifiedException extends DomainException {

    public CompetitorDisqualifiedException() {
        super(ErrorEnum.COMPETITOR_DISQUALIFIED);
    }
}
