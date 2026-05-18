package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageDetailResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchCompetitions implements SecuredCompetitionsFetchAllApiDelegate {

    private final GetCompetitionListServiceCase getCompetitionListServiceCase;
    private final UserInfoDTO userDetails;

    public FetchCompetitions(GetCompetitionListServiceCase getCompetitionListServiceCase, UserInfoDTO userDetails) {
        this.getCompetitionListServiceCase = getCompetitionListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<CompetitionResponseDTO>> fetchCompetitionsSecured() {
        return ResponseEntity.ok(
                getCompetitionListServiceCase.getCompetitions(userDetails.getEmail(), userDetails.isOrganizer()).stream()
                        .map(competition -> new CompetitionResponseDTO(
                                competition.id(),
                                competition.name(),
                                competition.description(),
                                competition.country(),
                                competition.status(),
                                competition.address(),
                                competition.stages().stream()
                                        .map(stage -> new CompetitionStageDetailResponseDTO(
                                                stage.dateFrom() != null ? stage.dateFrom().intValue() : null,
                                                stage.dateTo() != null ? stage.dateTo().intValue() : null,
                                                stage.id(),
                                                stage.name()
                                        ))
                                        .toList(),
                                List.of()
                        ))
                        .toList()
        );
    }
}
