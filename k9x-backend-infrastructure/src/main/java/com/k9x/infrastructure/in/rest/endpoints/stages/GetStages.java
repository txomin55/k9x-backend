package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.stages.use_case.GetStageListServiceCase;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.StagesFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionLocationDetailResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageSummaryResponseDTO;
import com.k9x.oas.stub.model.StageEventSummaryResponseDTO;
import com.k9x.oas.stub.model.StageNotificationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class GetStages implements StagesFetchAllApiDelegate {

    private final GetStageListServiceCase getStageListServiceCase;
    private final ReferenceNameResolver referenceNames;

    public GetStages(GetStageListServiceCase getStageListServiceCase, ReferenceNameResolver referenceNames) {
        this.getStageListServiceCase = getStageListServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<List<StageSummaryResponseDTO>> fetchAllStages(Long from, Long to) {
        return ResponseEntity.ok(
                getStageListServiceCase.getStages(from, to).stream()
                        .map(stage -> new StageSummaryResponseDTO(
                                stage.id(),
                                stage.name(),
                                stage.competitionName(),
                                stage.country(),
                                new CompetitionLocationDetailResponseDTO(
                                        stage.address(),
                                        stage.coordAlt() != null ? BigDecimal.valueOf(stage.coordAlt()) : null,
                                        stage.coordLong() != null ? BigDecimal.valueOf(stage.coordLong()) : null),
                                stage.dateFrom(),
                                stage.dateTo(),
                                stage.events().stream()
                                        .map(e -> new StageEventSummaryResponseDTO(
                                                e.id(),
                                                e.name(),
                                                referenceNames.discipline(e.disciplineId()),
                                                e.competitorCount(),
                                                e.status(),
                                                e.enrollmentOpened(),
                                                e.enrollmentDeadline(),
                                                e.awards().stream().map(a -> new IdNameDTO(a, a)).toList(),
                                                e.rank()))
                                        .toList(),
                                stage.notifications().stream()
                                        .map(n -> new StageNotificationResponseDTO(
                                                n.timestamp(), n.eventIds(), n.content()))
                                        .toList(),
                                stage.status(),
                                stage.organizer(),
                                stage.includesRankings()))
                        .toList());
    }
}
