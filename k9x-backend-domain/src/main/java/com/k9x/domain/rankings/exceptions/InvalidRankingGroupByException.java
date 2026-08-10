package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class InvalidRankingGroupByException extends DomainException {

    public InvalidRankingGroupByException() {
        super(ErrorEnum.RANKING_GROUP_BY_INVALID);
    }
}
