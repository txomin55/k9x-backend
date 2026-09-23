package com.k9x.infrastructure.in.rest.endpoints.extractions;

import com.k9x.application.extractions.use_case.GetExtractionLogServiceCase;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.ExtractionsFetchAllApiDelegate;
import com.k9x.oas.stub.model.ExtractionLogDayResponseDTO;
import com.k9x.oas.stub.model.ExtractionLogEventResponseDTO;
import com.k9x.oas.stub.model.ExtractionLogStageResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Public log of imported trials. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}, so the
 * auth filter lets anonymous requests through.
 */
public class FetchExtractionLog implements ExtractionsFetchAllApiDelegate {

    private final GetExtractionLogServiceCase getExtractionLogServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchExtractionLog(GetExtractionLogServiceCase getExtractionLogServiceCase,
                              ReferenceNameResolver referenceNames) {
        this.getExtractionLogServiceCase = getExtractionLogServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<List<ExtractionLogDayResponseDTO>> fetchExtractionLog() {
        return ResponseEntity.ok(
                getExtractionLogServiceCase.getExtractionLog().stream()
                        .map(day -> new ExtractionLogDayResponseDTO(
                                day.date(),
                                day.stages().stream()
                                        .map(stage -> new ExtractionLogStageResponseDTO(
                                                stage.id(),
                                                stage.name(),
                                                stage.competitionName(),
                                                stage.country(),
                                                stage.dateFrom(),
                                                stage.dateTo(),
                                                stage.events().stream()
                                                        .map(e -> new ExtractionLogEventResponseDTO(
                                                                e.id(),
                                                                e.name(),
                                                                referenceNames.discipline(e.disciplineId()),
                                                                e.competitorCount(),
                                                                e.rank()))
                                                        .toList()))
                                        .toList()))
                        .toList());
    }
}
