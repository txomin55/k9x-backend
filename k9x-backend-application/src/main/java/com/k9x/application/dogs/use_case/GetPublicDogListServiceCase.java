package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.GetPublicDogListPersistencePort;
import com.k9x.application.dogs.port.payload.PublicDogListFilter;
import com.k9x.application.dogs.port.payload.PublicDogListPage;
import com.k9x.application.dogs.use_case.command.GetPublicDogListCommand;
import com.k9x.application.dogs.use_case.dto.PublicDogDTO;
import com.k9x.application.dogs.use_case.dto.PublicDogListDTO;
import com.k9x.domain.dogs.rank.DogRankIndex;

import java.util.List;

/**
 * The public dog directory. It asserts nothing about the caller: the endpoint sits outside {@code /secured/},
 * so there may well be no user behind the request, and every dog is listed to everyone.
 */
public class GetPublicDogListServiceCase {

    private final GetPublicDogListPersistencePort getPublicDogListPersistencePort;

    public GetPublicDogListServiceCase(GetPublicDogListPersistencePort getPublicDogListPersistencePort) {
        this.getPublicDogListPersistencePort = getPublicDogListPersistencePort;
    }

    public PublicDogListDTO getDogs(GetPublicDogListCommand command) {
        PublicDogListFilter filter = PublicDogListFilter.from(command);
        PublicDogListPage page = getPublicDogListPersistencePort.getDogs(filter);

        List<PublicDogDTO> items = page.dogs().stream()
                .map(dog -> new PublicDogDTO(
                        dog.identification(),
                        dog.name(),
                        dog.handler(),
                        dog.country(),
                        dog.sex(),
                        dog.breed(),
                        rank(dog.rank())))
                .toList();

        return PublicDogListDTO.of(items, filter, page.total());
    }

    /**
     * A dog only gets a history record once it has competed, so an absent index is not a low one: it is said
     * out loud instead of being flattened into a number.
     */
    private String rank(Integer rank) {
        return rank == null ? DogRankIndex.NOT_GENERATED : String.valueOf(rank);
    }
}
