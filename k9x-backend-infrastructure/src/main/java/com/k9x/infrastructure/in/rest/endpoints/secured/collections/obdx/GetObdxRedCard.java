package com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx;

import com.k9x.application.collections.obdx.use_case.GetObdxRedCardServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxRedCardDTO;
import com.k9x.oas.stub.api.SecuredEventsFetchRedCardObdxApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.RedCardResponseDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

public class GetObdxRedCard implements SecuredEventsFetchRedCardObdxApiDelegate {

    private final GetObdxRedCardServiceCase getObdxRedCardServiceCase;
    private final MessageSource messageSource;

    public GetObdxRedCard(GetObdxRedCardServiceCase getObdxRedCardServiceCase, MessageSource messageSource) {
        this.getObdxRedCardServiceCase = getObdxRedCardServiceCase;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<RedCardResponseDTO> fetchRedCard(String eventId, String competitorId) {
        FetchObdxRedCardDTO redCard = getObdxRedCardServiceCase.getRedCard(eventId, competitorId);
        return ResponseEntity.ok(redCard == null ? null : toResponse(redCard));
    }

    private RedCardResponseDTO toResponse(FetchObdxRedCardDTO redCard) {
        return new RedCardResponseDTO(
                new IdNameDTO(resolveExerciseName(redCard.exerciseId()), redCard.exerciseId()),
                new IdNameDTO(redCard.judgeName(), redCard.judgeId()),
                redCard.timestamp());
    }

    private String resolveExerciseName(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }
        return messageSource.getMessage(exerciseId, null, exerciseId, LocaleContextHolder.getLocale());
    }
}
