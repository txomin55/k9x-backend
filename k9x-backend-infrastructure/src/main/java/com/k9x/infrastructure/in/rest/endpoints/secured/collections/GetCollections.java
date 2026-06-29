package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCollectionsFecthAllApiDelegate;
import com.k9x.oas.stub.model.CollectionsResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetCollections implements SecuredCollectionsFecthAllApiDelegate {

    private final GetCollectionListServiceCase getCollectionListServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public GetCollections(GetCollectionListServiceCase getCollectionListServiceCase, UserInfoDTO userDetails,
                          MessageSource messageSource) {
        this.getCollectionListServiceCase = getCollectionListServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<List<CollectionsResponseDTO>> fetchCollectionsSecured() {
        return ResponseEntity.ok(
                getCollectionListServiceCase.getCollections(userDetails.getEmail()).stream()
                        .map(collection -> new CollectionsResponseDTO(
                                collection.competitionName(),
                                collection.stageName(),
                                collection.eventName(),
                                collection.eventId(),
                                collection.status(),
                                collection.judges().stream()
                                        .map(judge -> new IdNameDTO(judge.name(), judge.id()))
                                        .toList(),
                                resolveDiscipline(collection.discipline())
                        ))
                        .toList()
        );
    }

    private IdNameDTO resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "discipline." + disciplineId.toUpperCase(Locale.ROOT) + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}
