package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.rankings.use_case.GetEventRankingsServiceCase;
import com.k9x.infrastructure.in.rest.configuration.session.OptionalRequestUser;
import com.k9x.oas.stub.api.StagesFetchEventRankingsApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Public, but aware of who is asking: the auth filter resolves a token on public paths when one is sent, and
 * that decides whether standalone ranking identifiers are handed out.
 */
public class GetEventRankings implements StagesFetchEventRankingsApiDelegate {

    private final GetEventRankingsServiceCase getEventRankingsServiceCase;
    private final OptionalRequestUser requestUser;

    public GetEventRankings(GetEventRankingsServiceCase getEventRankingsServiceCase,
                            OptionalRequestUser requestUser) {
        this.getEventRankingsServiceCase = getEventRankingsServiceCase;
        this.requestUser = requestUser;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchEventRankings(String id, String eventId) {
        return ResponseEntity.ok(
                getEventRankingsServiceCase
                        .getEventRankings(eventId, requestUser.isAuthenticated()).stream()
                        // The generated constructor is positional: (name, id).
                        .map(ranking -> new IdNameDTO(ranking.name(), ranking.id()))
                        .toList());
    }
}
