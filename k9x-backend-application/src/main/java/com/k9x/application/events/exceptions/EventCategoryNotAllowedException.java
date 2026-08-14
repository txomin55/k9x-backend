package com.k9x.application.events.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * The event declares a category its configuration does not admit — only the grade hosting the world
 * championship accepts the {@code WC_*} rounds; every other grade is limited to {@code CLUB} and {@code OPEN}.
 */
public class EventCategoryNotAllowedException extends DomainException {

    public EventCategoryNotAllowedException() {
        super(ErrorEnum.EVENT_CATEGORY_NOT_ALLOWED);
    }
}
