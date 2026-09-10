package com.k9x.application.dogs.port;

import com.k9x.application.dogs.port.payload.PublicDogListFilter;
import com.k9x.application.dogs.port.payload.PublicDogListPage;

public interface GetPublicDogListPersistencePort {

    PublicDogListPage getDogs(PublicDogListFilter filter);
}
