package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.GetObdxYellowCardsServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxYellowCardDTO;
import com.k9x.oas.stub.api.SecuredEventsFetchYellowCardsObdxApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.YellowCardResponseDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetObdxYellowCards implements SecuredEventsFetchYellowCardsObdxApiDelegate {

    private final GetObdxYellowCardsServiceCase getObdxYellowCardsServiceCase;
    private final MessageSource messageSource;

    public GetObdxYellowCards(GetObdxYellowCardsServiceCase getObdxYellowCardsServiceCase, MessageSource messageSource) {
        this.getObdxYellowCardsServiceCase = getObdxYellowCardsServiceCase;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<List<YellowCardResponseDTO>> fetchYellowCards(String eventId, String competitorId) {
        List<YellowCardResponseDTO> response = getObdxYellowCardsServiceCase.getYellowCards(eventId, competitorId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private YellowCardResponseDTO toResponse(FetchObdxYellowCardDTO yellowCard) {
        return new YellowCardResponseDTO(
                new IdNameDTO(resolveExerciseName(yellowCard.exerciseId()), yellowCard.exerciseId()),
                new IdNameDTO(yellowCard.judgeName(), yellowCard.judgeId()),
                yellowCard.timestamp());
    }

    private String resolveExerciseName(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }
        return messageSource.getMessage(exerciseId, null, exerciseId, LocaleContextHolder.getLocale());
    }
}
