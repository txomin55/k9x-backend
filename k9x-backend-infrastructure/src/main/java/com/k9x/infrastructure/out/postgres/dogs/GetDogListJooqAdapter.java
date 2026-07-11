package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

public class GetDogListJooqAdapter implements GetDogListPersistencePort {

    private final DSLContext dsl;

    public GetDogListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Dog> getDogs(String owner, String creator) {
        Condition ownership = DSL.noCondition();
        if (owner != null) {
            ownership = ownership.or(Tables.DOGS.OWNER.eq(owner));
        }
        if (creator != null) {
            ownership = ownership.or(Tables.DOGS.CREATOR.eq(creator));
        }
        return dsl.select()
                .from(Tables.DOGS)
                .where(ownership)
                .and(Tables.DOGS.DELETED_AT.isNull())
                .fetch(r -> new Dog(
                        r.get(Tables.DOGS.ID),
                        r.get(Tables.DOGS.IDENTITY),
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
                ));
    }
}
