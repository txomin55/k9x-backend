package com.k9x.infrastructure.in.rest.endpoints.dog;

import com.k9x.application.dog.dto.DogListDTO;
import com.k9x.application.dog.port.GetDogListServiceCase;
import com.k9x.oas.stub.api.GetDogsApiDelegate;
import com.k9x.oas.stub.model.GetDogListWeb;
import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetDogList implements GetDogsApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final AuthorizationExtractor tokenExtractor;

    public GetDogList(GetDogListServiceCase getDogListService, AuthorizationExtractor tokenExtractor) {
        this.getDogListService = getDogListService;
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    public ResponseEntity<List<GetDogListWeb>> getDogs(String authorization) {
        RequestUserDetails user = tokenExtractor.getDataFromToken(authorization);

        List<DogListDTO> dogs = getDogListService.getDogs(user.getId());

        List<GetDogListWeb> mappedDogs = dogs.stream()
                .map(dog -> new GetDogListWeb(dog.id(), dog.name(), dog.image()))
                .toList();

        return ResponseEntity.ok(mappedDogs);
    }
}
