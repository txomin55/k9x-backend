package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateDogJooqAdapter implements UpdateDogPersistencePort {

    private final DSLContext dsl;

    public UpdateDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateDog(String id, String name, String image, String breed, String identity,
                          String owner, String team, String country, long lastUpdate) {
        dsl.update(Tables.DOGS)
                .set(Tables.DOGS.NAME, name)
                .set(Tables.DOGS.IMAGE, image)
                .set(Tables.DOGS.BREED, breed)
                .set(Tables.DOGS.IDENTITY, identity)
                .set(Tables.DOGS.OWNER, owner)
                .set(Tables.DOGS.TEAM, team)
                .set(Tables.DOGS.COUNTRY, country)
                .set(Tables.DOGS.LAST_UPDATE, lastUpdate)
                .where(Tables.DOGS.ID.eq(id))
                .execute();
    }
}
