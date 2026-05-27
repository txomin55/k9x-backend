package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventConfigurationIdRequiredException extends DomainException {

    public EventConfigurationIdRequiredException() {
        super(ErrorEnum.EVENT_CONFIGURATION_ID_REQUIRED);
    }
}
