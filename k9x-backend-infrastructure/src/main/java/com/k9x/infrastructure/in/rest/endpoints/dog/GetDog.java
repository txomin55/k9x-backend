package com.k9x.infrastructure.in.rest.endpoints.dog;

import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.application.dog.action.GetDogServiceCase;
import com.k9x.application.dog.command.DogGetCommand;
import com.k9x.application.dog.dto.DogDTO;
import com.k9x.oas.stub.api.GetDogApiDelegate;
import com.k9x.oas.stub.model.GetDogWeb;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GetDog implements GetDogApiDelegate {

    private final GetDogServiceCase getDogServiceCase;
    private final AuthTokenDTO userDetails;

    public GetDog(GetDogServiceCase getDogServiceCase, AuthTokenDTO userDetails) {
        this.getDogServiceCase = getDogServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<GetDogWeb> getDog(String id) {
        DogDTO dog = getDogServiceCase.getDog(new DogGetCommand(id, userDetails.getSubject()));

        return ResponseEntity.ok(new GetDogWeb(dog.id(), dog.name(), dog.image()));
    }
}
