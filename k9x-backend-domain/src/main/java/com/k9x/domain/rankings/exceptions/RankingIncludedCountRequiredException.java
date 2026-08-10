package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * Thrown when an inclusion criterion other than NONE comes without a positive included count.
 */
public class RankingIncludedCountRequiredException extends DomainException {

    public RankingIncludedCountRequiredException() {
        super(ErrorEnum.RANKING_INCLUDED_COUNT_REQUIRED);
    }
}
