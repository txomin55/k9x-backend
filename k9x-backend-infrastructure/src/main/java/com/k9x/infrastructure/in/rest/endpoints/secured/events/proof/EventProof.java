package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import com.k9x.application.events.exceptions.EventProofNotSupportedException;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsEventProofApiDelegate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serves the printable working-booklet proof of one competitor in one event.
 *
 * <p>Deliberately layout-free: the booklet is a per-discipline paper form, so this endpoint only fetches the
 * event — which is where the organizer check lives, inside {@link GetEventServiceCase} — picks the
 * {@link EventProofRenderer} registered for its discipline and ships whatever bytes come back. Adding a
 * discipline means adding a renderer, not touching this class.
 */
public class EventProof implements SecuredEventsEventProofApiDelegate {

    private final GetEventServiceCase getEventServiceCase;
    private final UserInfoDTO userDetails;
    private final Map<String, EventProofRenderer> renderersByDiscipline;

    public EventProof(GetEventServiceCase getEventServiceCase, UserInfoDTO userDetails,
                      List<EventProofRenderer> renderers) {
        this.getEventServiceCase = getEventServiceCase;
        this.userDetails = userDetails;
        this.renderersByDiscipline = renderers.stream()
                .collect(Collectors.toMap(EventProofRenderer::discipline, Function.identity(),
                        (first, _) -> first, LinkedHashMap::new));
    }

    @Override
    public ResponseEntity<Resource> getEventProofSecured(String eventId, String competitorId) {
        FetchEventDetailDTO event = getEventServiceCase.getEvent(eventId, userDetails.getEmail(),
                userDetails.isOrganizer());
        EventProofDocument proof = renderer(event).render(eventId, competitorId, event);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(proof.content().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(proof.fileName(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(new ByteArrayResource(proof.content()));
    }

    /**
     * The event detail only carries discipline data for the disciplines it knows how to read, so an event of
     * any other discipline arrives empty — indistinguishable from, and treated the same as, a discipline with
     * no registered renderer: there is no proof to hand out.
     */
    private EventProofRenderer renderer(FetchEventDetailDTO event) {
        String discipline = event.obdx() == null ? null : event.obdx().discipline();
        EventProofRenderer renderer = discipline == null ? null : renderersByDiscipline.get(discipline);
        if (renderer == null) {
            throw new EventProofNotSupportedException(discipline);
        }
        return renderer;
    }
}
