package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.oas.stub.api.SecuredCollectionsFetchOneApiDelegate;
import com.k9x.oas.stub.model.CollectionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GetCollection implements SecuredCollectionsFetchOneApiDelegate {

    @Override
    public ResponseEntity<CollectionResponseDTO> fetchOneCollection(String id) {
        return ResponseEntity.ok(new CollectionResponseDTO());
    }
}
