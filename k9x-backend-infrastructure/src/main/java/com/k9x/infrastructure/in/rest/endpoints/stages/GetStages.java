package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.stages.use_case.GetStageListServiceCase;
import com.k9x.oas.stub.api.StagesFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionLocationDetailResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageSummaryResponseDTO;
import com.k9x.oas.stub.model.StageEventSummaryResponseDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class GetStages implements StagesFetchAllApiDelegate {

    private final GetStageListServiceCase getStageListServiceCase;
    private final MessageSource messageSource;

    public GetStages(GetStageListServiceCase getStageListServiceCase, MessageSource messageSource) {
        this.getStageListServiceCase = getStageListServiceCase;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<List<StageSummaryResponseDTO>> fetchAllStages() {
        return ResponseEntity.ok(
                getStageListServiceCase.getStages().stream()
                        .map(stage -> new StageSummaryResponseDTO(
                                stage.id(),
                                stage.name(),
                                stage.description(),
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
                                                resolveDiscipline(e.disciplineId()),
                                                e.competitorCount(),
                                                e.status(),
                                                e.enrollmentOpened(),
                                                e.enrollmentDeadline()))
                                        .toList(),
                                stage.status(),
                                stage.organizer()))
                        .toList());
    }

    private IdNameDTO resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "discipline." + disciplineId + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}
