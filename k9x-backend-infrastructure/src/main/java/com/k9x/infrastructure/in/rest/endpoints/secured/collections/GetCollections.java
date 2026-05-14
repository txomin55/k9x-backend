package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.oas.stub.api.SecuredCollectionsFecthAllApiDelegate;
import com.k9x.oas.stub.model.CollectionsResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCollections implements SecuredCollectionsFecthAllApiDelegate {

    @Override
    public ResponseEntity<List<CollectionsResponseDTO>> fetchCollectionsSecured() {
        return ResponseEntity.ok(List.of(
                new CollectionsResponseDTO(
                        "Mocked Competition",
                        "Mocked Stage",
                        "Mocked Event",
                        "event-1",
                        "OPEN",
                        List.of(new IdNameDTO("Judge One", "judge-1"))
                )
        ));
    }
}
