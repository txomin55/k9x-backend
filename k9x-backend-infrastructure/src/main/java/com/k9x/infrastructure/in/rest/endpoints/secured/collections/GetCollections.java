package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.SecuredCollectionsFecthAllApiDelegate;
import com.k9x.oas.stub.model.CollectionsResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetCollections implements SecuredCollectionsFecthAllApiDelegate {

    private final GetCollectionListServiceCase getCollectionListServiceCase;
    private final UserInfoDTO userDetails;
    private final ReferenceNameResolver referenceNames;

    public GetCollections(GetCollectionListServiceCase getCollectionListServiceCase, UserInfoDTO userDetails,
                          ReferenceNameResolver referenceNames) {
        this.getCollectionListServiceCase = getCollectionListServiceCase;
        this.userDetails = userDetails;
        this.referenceNames = referenceNames;
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
                                referenceNames.discipline(collection.discipline())
                        ))
                        .toList()
        );
    }
}
