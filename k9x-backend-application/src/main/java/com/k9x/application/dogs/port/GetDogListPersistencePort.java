package com.k9x.application.dogs.port;

import com.k9x.application.dogs.port.payload.DogListFilter;
import com.k9x.application.dogs.port.payload.DogListPage;

public interface GetDogListPersistencePort {

    DogListPage getDogs(DogListFilter filter);
}
