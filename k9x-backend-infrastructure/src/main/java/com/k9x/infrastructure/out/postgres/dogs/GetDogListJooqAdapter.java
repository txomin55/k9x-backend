package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.payload.DogListFilter;
import com.k9x.application.dogs.port.payload.DogListPage;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.List;

public class GetDogListJooqAdapter implements GetDogListPersistencePort {

    private final DSLContext dsl;

    public GetDogListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public DogListPage getDogs(DogListFilter filter) {
        SelectConditionStep<?> query = dsl.select()
                .from(Tables.DOGS)
                .where(ownership(filter))
                .and(Tables.DOGS.DELETED_AT.isNull())
                .and(textSearch(filter))
                .and(country(filter));

        if (!filter.paginated()) {
            List<Dog> dogs = query.orderBy(Tables.DOGS.NAME.asc(), Tables.DOGS.IDENTIFICATION.asc()).fetch(GetDogListJooqAdapter::toDog);
            return new DogListPage(dogs, dogs.size());
        }

        // Paginated reads need the full match count, which the page itself cannot tell.
        Integer total = dsl.selectCount()
                .from(Tables.DOGS)
                .where(ownership(filter))
                .and(Tables.DOGS.DELETED_AT.isNull())
                .and(textSearch(filter))
                .and(country(filter))
                .fetchOne(0, int.class);
        List<Dog> dogs = query.orderBy(Tables.DOGS.NAME.asc(), Tables.DOGS.IDENTIFICATION.asc())
                .limit(filter.limit())
                .offset(filter.offset())
                .fetch(GetDogListJooqAdapter::toDog);
        return new DogListPage(dogs, total == null ? dogs.size() : total);
    }

    private Condition ownership(DogListFilter filter) {
        Condition ownership = DSL.noCondition();
        if (filter.owner() != null) {
            ownership = ownership.or(Tables.DOGS.OWNER.eq(filter.owner()));
        }
        if (filter.creator() != null) {
            ownership = ownership.or(Tables.DOGS.CREATOR.eq(filter.creator()));
        }
        return ownership;
    }

    private Condition country(DogListFilter filter) {
        if (filter.country() == null) {
            return DSL.noCondition();
        }
        return Tables.DOGS.COUNTRY.eq(filter.country());
    }

    /**
     * The name and the identification searches are the two ends of a single search box, so a dog is
     * listed when either of the two matches, not only when both do.
     */
    private Condition textSearch(DogListFilter filter) {
        Condition search = DSL.noCondition();
        if (filter.nameContains() != null) {
            search = search.or(Tables.DOGS.NAME.containsIgnoreCase(filter.nameContains()));
        }
        if (filter.identificationContains() != null) {
            search = search.or(Tables.DOGS.IDENTIFICATION.containsIgnoreCase(filter.identificationContains()));
        }
        return search;
    }

    private static Dog toDog(org.jooq.Record r) {
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
                r.get(Tables.DOGS.DELETED_AT));
    }
}
