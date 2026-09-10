package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.GetPublicDogListPersistencePort;
import com.k9x.application.dogs.port.payload.PublicDogListFilter;
import com.k9x.application.dogs.port.payload.PublicDogListPage;
import com.k9x.application.dogs.use_case.dto.FetchPublicDogDTO;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogIndexHistory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public class GetPublicDogListJooqAdapter implements GetPublicDogListPersistencePort {

    private static final SnapDogIndexHistory HISTORY = Tables.SNAP_DOG_INDEX_HISTORY;
    private static final String LATEST_INDEX = "latest_index";

    private final DSLContext dsl;

    public GetPublicDogListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public PublicDogListPage getDogs(PublicDogListFilter filter) {
        Table<?> latestIndex = latestIndex();
        Field<Integer> rank = latestIndex.field(HISTORY.RANK);

        SelectConditionStep<?> query = dsl.select(
                        Tables.DOGS.IDENTIFICATION, Tables.DOGS.NAME, Tables.DOGS.HANDLER, Tables.DOGS.COUNTRY, Tables.DOGS.SEX,
                        Tables.DOGS.BREED, rank)
                .from(Tables.DOGS)
                .leftJoin(latestIndex)
                .on(latestIndex.field(HISTORY.DOG_IDENTIFICATION).eq(Tables.DOGS.IDENTIFICATION))
                .where(Tables.DOGS.DELETED_AT.isNull())
                .and(nameSearch(filter))
                .and(handlerSearch(filter))
                .and(country(filter));

        if (!filter.paginated()) {
            List<FetchPublicDogDTO> dogs = query.orderBy(order(rank)).fetch(record -> toDog(record, rank));
            return new PublicDogListPage(dogs, dogs.size());
        }

        // Paginated reads need the full match count, which the page itself cannot tell. Every filter is on the
        // dogs table, so counting does not need the index join.
        Integer total = dsl.selectCount()
                .from(Tables.DOGS)
                .where(Tables.DOGS.DELETED_AT.isNull())
                .and(nameSearch(filter))
                .and(handlerSearch(filter))
                .and(country(filter))
                .fetchOne(0, int.class);
        List<FetchPublicDogDTO> dogs = query.orderBy(order(rank))
                .limit(filter.limit())
                .offset(filter.offset())
                .fetch(record -> toDog(record, rank));
        return new PublicDogListPage(dogs, total == null ? dogs.size() : total);
    }

    /**
     * The dog's current index: the history is append-only, so the row that applies is the latest one per dog.
     * It is joined as a derived table rather than filtered in the {@code WHERE} so a dog that has never
     * competed still shows up, with no index.
     */
    private Table<?> latestIndex() {
        return dsl.select(HISTORY.DOG_IDENTIFICATION, HISTORY.RANK)
                .distinctOn(HISTORY.DOG_IDENTIFICATION)
                .from(HISTORY)
                .orderBy(HISTORY.DOG_IDENTIFICATION, HISTORY.APPLYING_TIMESTAMP.desc())
                .asTable(LATEST_INDEX);
    }

    /**
     * Best index first — the directory is read as a ranking — with the dogs that have none last rather than
     * ahead of every ranked dog. Name and identification break the tie so pages stay stable.
     */
    private OrderField<?>[] order(Field<Integer> rank) {
        return new OrderField<?>[]{
                rank.desc().nullsLast(), Tables.DOGS.NAME.asc(), Tables.DOGS.IDENTIFICATION.asc()};
    }

    private Condition nameSearch(PublicDogListFilter filter) {
        if (filter.nameContains() == null) {
            return DSL.noCondition();
        }
        return Tables.DOGS.NAME.containsIgnoreCase(filter.nameContains());
    }

    private Condition handlerSearch(PublicDogListFilter filter) {
        if (filter.handlerContains() == null) {
            return DSL.noCondition();
        }
        return Tables.DOGS.HANDLER.containsIgnoreCase(filter.handlerContains());
    }

    private Condition country(PublicDogListFilter filter) {
        if (filter.country() == null) {
            return DSL.noCondition();
        }
        return Tables.DOGS.COUNTRY.eq(filter.country());
    }

    private static FetchPublicDogDTO toDog(Record record, Field<Integer> rank) {
        String sex = record.get(Tables.DOGS.SEX);
        return new FetchPublicDogDTO(
                record.get(Tables.DOGS.IDENTIFICATION),
                record.get(Tables.DOGS.NAME),
                record.get(Tables.DOGS.HANDLER),
                record.get(Tables.DOGS.COUNTRY),
                sex == null ? null : Sex.valueOf(sex),
                record.get(Tables.DOGS.BREED),
                record.get(rank));
    }
}
