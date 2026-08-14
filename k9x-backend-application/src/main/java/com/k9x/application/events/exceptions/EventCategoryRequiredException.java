package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class EventCategoryRequiredException extends DomainException {

    public EventCategoryRequiredException() {
        super(ErrorEnum.EVENT_CATEGORY_REQUIRED);
    }
}
