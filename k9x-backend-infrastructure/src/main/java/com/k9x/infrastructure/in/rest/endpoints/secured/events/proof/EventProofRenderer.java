package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;

/**
 * Produces the working-booklet proof of one competitor for one discipline.
 *
 * <p>The booklet is a per-discipline paper form: its boxes, its scoring vocabulary and even its page size
 * belong to the federation that prints it. So the endpoint owns no layout at all — it resolves the renderer
 * registered for the event's discipline and hands the work over. A discipline with no renderer simply has no
 * proof to offer.
 */
public interface EventProofRenderer {

    /** Discipline this renderer prints proofs for, as stored on the event (see {@code Discipline}). */
    String discipline();

    /**
     * @param competitorId the competitor's dog identification.
     * @throws com.k9x.domain.exceptions.NotFoundResourceException when the competitor is not in the event.
     */
    EventProofDocument render(String eventId, String competitorId, FetchEventDetailDTO event);
}
