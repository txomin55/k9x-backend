package com.k9x.infrastructure.out.postgres.dogs;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_RANK;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogRankDogIdentificationsJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private DSLContext dslReturning(String... dogs) {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            bindings.add(List.of(ctx.bindings()));
            Result<Record1<String>> result = DSL.using(SQLDialect.POSTGRES).newResult(SNAP_DOG_RANK.DOG_IDENTIFICATION);
            for (String dog : dogs) {
                Record1<String> record = DSL.using(SQLDialect.POSTGRES).newRecord(SNAP_DOG_RANK.DOG_IDENTIFICATION);
                record.set(SNAP_DOG_RANK.DOG_IDENTIFICATION, dog);
                result.add(record);
            }
            return new MockResult[]{new MockResult(dogs.length, result)};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void the_first_page_starts_at_the_first_dog() {
        List<String> dogs = new GetDogRankDogIdentificationsJooqAdapter(dslReturning("dog-1", "dog-2"))
                .getDogIdentifications(null, 200);

        assertThat(dogs).containsExactly("dog-1", "dog-2");
        assertThat(sqls.getFirst())
                .contains("select distinct \"k9x\".\"snap_dog_rank\".\"dog_identification\"")
                .doesNotContain(">")
                .contains("order by \"k9x\".\"snap_dog_rank\".\"dog_identification\"")
                .contains("fetch next ? rows only");
        assertThat(bindings.getFirst()).contains(200L);
    }

    @Test
    void a_following_page_continues_strictly_after_the_last_dog_of_the_previous_one() {
        new GetDogRankDogIdentificationsJooqAdapter(dslReturning()).getDogIdentifications("dog-2", 200);

        assertThat(sqls.getFirst()).contains("\"k9x\".\"snap_dog_rank\".\"dog_identification\" > ?");
        assertThat(bindings.getFirst()).contains("dog-2");
    }
}
