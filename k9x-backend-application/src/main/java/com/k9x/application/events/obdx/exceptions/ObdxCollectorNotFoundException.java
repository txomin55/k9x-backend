package com.k9x.application.events.obdx.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class ObdxCollectorNotFoundException extends NotFoundResourceException {

    public ObdxCollectorNotFoundException(String collectorEmail) {
        super(ErrorEnum.COLLECTOR_NOT_FOUND, new String[]{collectorEmail});
    }
}
