package com.k9x.domain.events.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class CompetitorNotFoundException extends NotFoundResourceException {

    public CompetitorNotFoundException() {
        super(ErrorEnum.COMPETITOR_NOT_FOUND);
    }
}
