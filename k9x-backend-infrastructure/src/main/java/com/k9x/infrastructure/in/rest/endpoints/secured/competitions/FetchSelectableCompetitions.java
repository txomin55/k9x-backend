package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.GetSelectableCompetitionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsFetchSelectableApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.SelectableCompetitionResponseDTO;
import com.k9x.oas.stub.model.SelectableStageResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchSelectableCompetitions implements SecuredCompetitionsFetchSelectableApiDelegate {

    private final GetSelectableCompetitionListServiceCase getSelectableCompetitionListServiceCase;
    private final UserInfoDTO userDetails;

    public FetchSelectableCompetitions(
            GetSelectableCompetitionListServiceCase getSelectableCompetitionListServiceCase,
            UserInfoDTO userDetails) {
        this.getSelectableCompetitionListServiceCase = getSelectableCompetitionListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<SelectableCompetitionResponseDTO>> fetchSelectableCompetitionsSecured() {
        return ResponseEntity.ok(
                getSelectableCompetitionListServiceCase
                        .getSelectableCompetitions(userDetails.getEmail(), userDetails.isOrganizer()).stream()
                        .map(competition -> new SelectableCompetitionResponseDTO(
                                competition.id(),
                                competition.name(),
                                competition.stages().stream()
                                        .map(stage -> new SelectableStageResponseDTO(
                                                stage.id(),
                                                stage.name(),
                                                stage.events().stream()
                                                        // The generated constructor is positional: (name, id).
                                                        .map(event -> new IdNameDTO(event.name(), event.id()))
                                                        .toList()))
                                        .toList()))
                        .toList());
    }
}
