package com.k9x.application.breeds.port;

import com.k9x.application.breeds.use_case.dto.BreedDTO;

import java.util.List;

public interface GetBreedListPort {

    List<BreedDTO> getBreeds();
}
