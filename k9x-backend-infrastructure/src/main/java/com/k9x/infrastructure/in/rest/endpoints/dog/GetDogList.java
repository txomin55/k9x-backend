package com.k9x.infrastructure.in.rest.endpoints.dog;

import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.application.dog.action.GetDogListServiceCase;
import com.k9x.application.dog.dto.DogListDTO;
import com.k9x.oas.stub.api.GetDogsApiDelegate;
import com.k9x.oas.stub.model.GetDogListWeb;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetDogList implements GetDogsApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final AuthTokenDTO userDetails;

    public GetDogList(GetDogListServiceCase getDogListService, AuthTokenDTO userDetails) {
        this.getDogListService = getDogListService;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<GetDogListWeb>> getDogs() {
        List<DogListDTO> dogs = getDogListService.getDogs(userDetails.getSubject());

        List<GetDogListWeb> mappedDogs = dogs.stream()
                .map(dog -> new GetDogListWeb(dog.id(), dog.name(), dog.image()))
                .toList();

        return ResponseEntity.ok(mappedDogs);
    }
}
