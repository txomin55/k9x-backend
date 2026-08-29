package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.SecuredCompetitionsFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageDetailResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageEventDetailResponseDTO;
import com.k9x.oas.stub.model.StageNotificationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class FetchCompetitions implements SecuredCompetitionsFetchAllApiDelegate {

    private final GetCompetitionListServiceCase getCompetitionListServiceCase;
    private final UserInfoDTO userDetails;
    private final ReferenceNameResolver referenceNames;

    public FetchCompetitions(GetCompetitionListServiceCase getCompetitionListServiceCase, UserInfoDTO userDetails,
                             ReferenceNameResolver referenceNames) {
        this.getCompetitionListServiceCase = getCompetitionListServiceCase;
        this.userDetails = userDetails;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<List<CompetitionResponseDTO>> fetchCompetitionsSecured(String country) {
        return ResponseEntity.ok(
                getCompetitionListServiceCase
                        .getCompetitions(userDetails.getEmail(), userDetails.isOrganizer(), country).stream()
                        .map(competition -> new CompetitionResponseDTO(
                                competition.id(),
                                competition.name(),
                                competition.description(),
                                competition.country(),
                                competition.status(),
                                competition.address(),
                                competition.stages().stream()
                                        .map(stage -> new CompetitionStageDetailResponseDTO(
                                                stage.dateFrom() != null ? BigDecimal.valueOf(stage.dateFrom()) : null,
                                                stage.dateTo() != null ? BigDecimal.valueOf(stage.dateTo()) : null,
                                                stage.id(),
                                                stage.name(),
                                                stage.status(),
                                                stage.events().stream()
                                                        .map(event -> new CompetitionStageEventDetailResponseDTO(
                                                                event.id(),
                                                                event.name(),
                                                                referenceNames.discipline(event.discipline()),
                                                                event.status(),
                                                                event.rank()))
                                                        .toList(),
                                                stage.notifications().stream()
                                                        .map(n -> new StageNotificationResponseDTO(
                                                                n.timestamp(), n.eventIds(), n.content()))
                                                        .toList()
                                        ))
                                        .toList(),
                                competition.source().name()
                        ))
                        .toList()
        );
    }
}
