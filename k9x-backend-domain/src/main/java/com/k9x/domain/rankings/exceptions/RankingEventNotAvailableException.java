package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * Thrown when one of the requested events does not exist or has been soft deleted.
 */
public class RankingEventNotAvailableException extends DomainException {

    public RankingEventNotAvailableException() {
        super(ErrorEnum.RANKING_EVENT_NOT_AVAILABLE);
    }
}
