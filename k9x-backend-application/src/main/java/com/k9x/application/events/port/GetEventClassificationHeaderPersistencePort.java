package com.k9x.application.events.port;

import com.k9x.application.events.use_case.dto.FetchEventClassificationHeaderDTO;

import java.util.Optional;

public interface GetEventClassificationHeaderPersistencePort {

    /** The event with its stage and competition, soft-deleted or not; empty when no such event exists. */
    Optional<FetchEventClassificationHeaderDTO> getHeader(String eventId);
}
