package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventNotFoundException extends NotFoundResourceException {

    public EventNotFoundException() {
        super(ErrorEnum.EVENT_NOT_FOUND);
    }
}
