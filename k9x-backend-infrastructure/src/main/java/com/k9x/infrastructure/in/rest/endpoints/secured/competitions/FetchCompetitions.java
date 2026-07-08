package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageDetailResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageEventDetailResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class FetchCompetitions implements SecuredCompetitionsFetchAllApiDelegate {

    private final GetCompetitionListServiceCase getCompetitionListServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public FetchCompetitions(GetCompetitionListServiceCase getCompetitionListServiceCase, UserInfoDTO userDetails,
                             MessageSource messageSource) {
        this.getCompetitionListServiceCase = getCompetitionListServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
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
                                                stage.dateFrom() != null ? BigDecimal.valueOf(stage.dateFrom()) : null,
                                                stage.dateTo() != null ? BigDecimal.valueOf(stage.dateTo()) : null,
                                                stage.id(),
                                                stage.name(),
                                                stage.status(),
                                                stage.events().stream()
                                                        .map(event -> new CompetitionStageEventDetailResponseDTO(
                                                                event.id(),
                                                                event.name(),
                                                                resolveDiscipline(event.discipline()),
                                                                event.status(),
                                                                event.rank()))
                                                        .toList()
                                        ))
                                        .toList(),
                                List.of()
                        ))
                        .toList()
        );
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
