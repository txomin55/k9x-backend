package com.k9x.domain.rankings;

import com.k9x.domain.rankings.exceptions.InvalidRankingIncludeByException;

import java.util.Arrays;

/**
 * Which competitor results a ranking counts. {@link #NONE} means every result counts, and is the only
 * value for which {@code includedCount} carries no meaning.
 */
public enum RankingIncludeBy {

    HIGHEST,
    LOWEST,
    NONE;

    public static RankingIncludeBy from(String value) {
        return Arrays.stream(values())
                .filter(includeBy -> includeBy.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(InvalidRankingIncludeByException::new);
    }

    public boolean includesAll() {
        return this == NONE;
    }
}
