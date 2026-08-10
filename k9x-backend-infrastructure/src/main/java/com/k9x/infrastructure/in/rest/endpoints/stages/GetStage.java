package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.stages.use_case.GetStageServiceCase;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.StagesFetchOneApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageDetailResponseDTO;
import com.k9x.oas.stub.model.StageEventDetailCompetitorResponseDTO;
import com.k9x.oas.stub.model.StageEventDetailResponseDTO;
import com.k9x.oas.stub.model.StageNotificationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetStage implements StagesFetchOneApiDelegate {

    private final GetStageServiceCase getStageServiceCase;
    private final ReferenceNameResolver referenceNames;

    public GetStage(GetStageServiceCase getStageServiceCase, ReferenceNameResolver referenceNames) {
        this.getStageServiceCase = getStageServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<StageDetailResponseDTO> fetchStage(String id) {
        var stage = getStageServiceCase.getStage(id);
        return ResponseEntity.ok(new StageDetailResponseDTO(
                stage.id(),
                stage.name(),
                stage.competitionName(),
                stage.dateFrom(),
                stage.dateTo(),
                stage.events().stream()
                        .map(e -> new StageEventDetailResponseDTO(
                                e.id(),
                                e.name(),
                                referenceNames.discipline(e.disciplineId()),
                                new IdNameDTO(e.configurationName(), e.configurationId()),
                                mapCompetitors(e.competitors()),
                                e.status(),
                                e.enrollmentOpened(),
                                e.enrollmentDeadline() != null ? e.enrollmentDeadline() : null,
                                e.awards().stream().map(a -> new IdNameDTO(a, a)).toList(),
                                e.rank()))
                        .toList(),
                stage.notifications().stream()
                        .map(n -> new StageNotificationResponseDTO(n.timestamp(), n.eventIds(), n.content()))
                        .toList(),
                stage.address(),
                stage.organizer(),
                stage.status()));
    }

    private List<StageEventDetailCompetitorResponseDTO> mapCompetitors(List<FetchStageDetailCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new StageEventDetailCompetitorResponseDTO(
                        new IdNameDTO(c.dogName(), c.dogIdentification()),
                        c.owner(),
                        c.handler(),
                        c.country(),
                        c.team(),
                        referenceNames.breed(c.breed()),
                        c.verified()))
                .toList();
    }
}
