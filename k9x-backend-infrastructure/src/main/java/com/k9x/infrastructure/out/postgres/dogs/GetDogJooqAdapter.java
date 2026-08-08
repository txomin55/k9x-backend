package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;

public class GetDogJooqAdapter implements GetDogPersistencePort {

    private final DSLContext dsl;

    public GetDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Dog getDog(String identification) {
        return dsl.select()
                .from(Tables.DOGS)
                .where(Tables.DOGS.IDENTIFICATION.eq(identification))
                .and(Tables.DOGS.DELETED_AT.isNull())
                .fetchOptional(GetDogJooqAdapter::mapDog)
                .orElse(null);
    }

    @Override
    public Dog getDogByOrigin(String origin) {
        return dsl.select()
                .from(Tables.DOGS)
                .where(Tables.DOGS.ORIGIN.eq(origin))
                .and(Tables.DOGS.DELETED_AT.isNull())
                .fetchOptional(GetDogJooqAdapter::mapDog)
                .orElse(null);
    }

    private static Dog mapDog(Record r) {
        return new Dog(
                r.get(Tables.DOGS.IDENTIFICATION),
                r.get(Tables.DOGS.ORIGIN),
                        r.get(Tables.DOGS.LICENSE),
                r.get(Tables.DOGS.BREED),
                r.get(Tables.DOGS.NAME),
                r.get(Tables.DOGS.IMAGE),
                r.get(Tables.DOGS.OWNER),
                r.get(Tables.DOGS.HANDLER),
                r.get(Tables.DOGS.CREATOR),
                r.get(Tables.DOGS.COUNTRY),
                r.get(Tables.DOGS.TEAM),
                r.get(Tables.DOGS.SEX) == null ? null : Sex.valueOf(r.get(Tables.DOGS.SEX)),
                r.get(Tables.DOGS.WITHERS_CM),
                r.get(Tables.DOGS.THREE_FCI_GENERATIONS_CONFIRMED),
                r.get(Tables.DOGS.LAST_UPDATE),
                r.get(Tables.DOGS.CREATED_AT),
                r.get(Tables.DOGS.DELETED_AT)
        );
    }
}
