package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class CollectorNotFoundException extends NotFoundResourceException {

    public CollectorNotFoundException(String collectorEmail) {
        super(ErrorEnum.COLLECTOR_NOT_FOUND, new String[]{collectorEmail});
    }
}
