package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetDogJooqAdapter implements GetDogPersistencePort {

    private final DSLContext dsl;

    public GetDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Dog getDog(String id) {
        return dsl.select()
                .from(Tables.DOGS)
                .where(Tables.DOGS.ID.eq(id))
                .fetchOptional(r -> new Dog(
                        r.get(Tables.DOGS.ID),
                        r.get(Tables.DOGS.IDENTITY),
                        r.get(Tables.DOGS.BREED),
                        r.get(Tables.DOGS.NAME),
                        r.get(Tables.DOGS.IMAGE),
                        r.get(Tables.DOGS.OWNER),
                        r.get(Tables.DOGS.CREATOR),
                        r.get(Tables.DOGS.COUNTRY),
                        r.get(Tables.DOGS.TEAM),
                        r.get(Tables.DOGS.LAST_UPDATE),
                        r.get(Tables.DOGS.CREATED_AT),
                        r.get(Tables.DOGS.DELETED_AT)
                )).orElse(null);
    }
}
