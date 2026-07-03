package com.k9x.application.breeds.use_case;

import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.breeds.use_case.dto.BreedDTO;

import java.util.List;

public class GetBreedListServiceCase {

    private final GetBreedListPort getBreedListPort;

    public GetBreedListServiceCase(GetBreedListPort getBreedListPort) {
        this.getBreedListPort = getBreedListPort;
    }

    public List<BreedDTO> getBreeds() {
        return getBreedListPort.getBreeds();
    }
}
