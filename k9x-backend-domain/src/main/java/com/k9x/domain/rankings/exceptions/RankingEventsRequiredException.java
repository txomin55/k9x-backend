package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * A ranking cannot exist without at least one event that is not deleted.
 */
public class RankingEventsRequiredException extends DomainException {

    public RankingEventsRequiredException() {
        super(ErrorEnum.RANKING_EVENTS_REQUIRED);
    }
}
