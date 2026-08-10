package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class InvalidRankingIncludeByException extends DomainException {

    public InvalidRankingIncludeByException() {
        super(ErrorEnum.RANKING_INCLUDE_BY_INVALID);
    }
}
