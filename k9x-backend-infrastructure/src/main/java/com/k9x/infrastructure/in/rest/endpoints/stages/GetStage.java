package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.stages.use_case.GetStageServiceCase;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.oas.stub.api.StagesFetchOneApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageDetailResponseDTO;
import com.k9x.oas.stub.model.StageEventDetailCompetitorResponseDTO;
import com.k9x.oas.stub.model.StageEventDetailResponseDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetStage implements StagesFetchOneApiDelegate {

    private final GetStageServiceCase getStageServiceCase;
    private final MessageSource messageSource;

    public GetStage(GetStageServiceCase getStageServiceCase, MessageSource messageSource) {
        this.getStageServiceCase = getStageServiceCase;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<StageDetailResponseDTO> fetchStage(String id) {
        var stage = getStageServiceCase.getStage(id);
        return ResponseEntity.ok(new StageDetailResponseDTO(
                stage.id(),
                stage.name(),
                stage.dateFrom(),
                stage.dateTo(),
                stage.events().stream()
                        // TODO: StageEventDetailResponseDTO needs a `boolean enrollmentOpened` field in the OAS
                        //  definition. Once regenerated, pass e.enrollmentOpened() (already computed in the
                        //  CompetitionSnapshot root aggregate against the current timestamp) to the DTO.
                        // TODO: StageEventDetailResponseDTO needs a `Long enrollmentDeadline` field in the OAS
                        //  definition. Once regenerated, pass e.enrollmentDeadline() (carried through from the
                        //  EventSnapshot aggregate) to the DTO.
                        .map(e -> new StageEventDetailResponseDTO(
                                e.id(),
                                e.name(),
                                resolveDiscipline(e.disciplineId()),
                                new IdNameDTO(e.configurationName(), e.configurationId()),
                                mapCompetitors(e.competitors()),
                                e.status()))
                        .toList(),
                List.of(),
                stage.address(),
                stage.organizer()));
    }

    private List<StageEventDetailCompetitorResponseDTO> mapCompetitors(List<FetchStageDetailCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new StageEventDetailCompetitorResponseDTO(
                        new IdNameDTO(c.dogName(), c.dogId()),
                        c.owner(),
                        c.country(),
                        c.team(),
                        c.breed()))
                .toList();
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
