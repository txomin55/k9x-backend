package com.k9x.application.dogs.rank.port;

import java.util.List;

public interface GetDogRankDogIdentificationsPersistencePort {

    /**
     * The next {@code limit} dogs holding at least one {@code k9x.snap_dog_rank} row, in identification order and
     * strictly after {@code after} ({@code null} for the first page): keyset pagination, so a page never skips or
     * repeats a dog however large the offset.
     */
    List<String> getDogIdentifications(String after, int limit);
}
