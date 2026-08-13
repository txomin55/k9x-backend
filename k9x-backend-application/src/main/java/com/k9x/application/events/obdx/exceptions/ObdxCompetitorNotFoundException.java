package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxCompetitorNotFoundException extends NotFoundResourceException {

    public ObdxCompetitorNotFoundException() {
        super(ErrorEnum.COMPETITOR_NOT_FOUND_IN_EVENT);
    }
}
