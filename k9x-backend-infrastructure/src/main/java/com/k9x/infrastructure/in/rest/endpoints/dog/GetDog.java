package com.k9x.infrastructure.in.rest.endpoints.dog;

import com.k9x.application.dog.command.DogGetCommand;
import com.k9x.application.dog.dto.DogDTO;
import com.k9x.application.dog.port.GetDogServiceCase;
import com.k9x.oas.stub.api.GetDogApiDelegate;
import com.k9x.oas.stub.model.GetDogWeb;
import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GetDog implements GetDogApiDelegate {

    private final GetDogServiceCase getDogService;
    private final AuthorizationExtractor tokenExtractor;

    public GetDog(GetDogServiceCase getDogService, AuthorizationExtractor tokenExtractor) {
        this.getDogService = getDogService;
        this.tokenExtractor = tokenExtractor;

    }

    @Override
    public ResponseEntity<GetDogWeb> getDog(String id, String authorization) {
        RequestUserDetails user = tokenExtractor.getDataFromToken(authorization);

        DogDTO dog = getDogService.getDog(new DogGetCommand(id, user.getId()));

        return ResponseEntity.ok(new GetDogWeb(dog.id(), dog.name(), dog.image()));
    }
}
