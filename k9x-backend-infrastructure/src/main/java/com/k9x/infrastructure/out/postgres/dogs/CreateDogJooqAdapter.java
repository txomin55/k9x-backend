package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateDogJooqAdapter implements CreateDogPersistencePort {

    private final DSLContext dsl;

    public CreateDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createDog(String id, String name, String image, String breed, String identity,
                          String owner, String creator, String team, String country, long createdAt) {
        dsl.insertInto(Tables.DOGS)
                .set(Tables.DOGS.ID, id)
                .set(Tables.DOGS.NAME, name)
                .set(Tables.DOGS.IMAGE, image)
                .set(Tables.DOGS.BREED, breed)
                .set(Tables.DOGS.IDENTITY, identity)
                .set(Tables.DOGS.OWNER, owner)
                .set(Tables.DOGS.CREATOR, creator)
                .set(Tables.DOGS.TEAM, team)
                .set(Tables.DOGS.COUNTRY, country)
                .set(Tables.DOGS.CREATED_AT, createdAt)
                .set(Tables.DOGS.LAST_UPDATE, createdAt)
                .execute();
    }
}
