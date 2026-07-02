package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.obdx.port.GetObdxCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import org.jooq.DSLContext;

import java.util.List;

public class GetObdxCollectionCompetitorsJooqAdapter implements GetObdxCollectionCompetitorsPersistencePort {

    private final DSLContext dsl;

    public GetObdxCollectionCompetitorsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCollectionCompetitorDTO> getCompetitors(String eventId) {
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = Tables.DOGS;

        return dsl.select(ec.DOG_ID, ec.POSITION, ec.VERIFIED, ec.NOT_COMPETING, ec.BIH,
                        d.NAME, d.IDENTITY, d.BREED, d.OWNER, d.HANDLER, d.TEAM, d.COUNTRY)
                .from(ec)
                .join(d).on(d.ID.eq(ec.DOG_ID).and(d.DELETED_AT.isNull()))
                .where(ec.EVENT_ID.eq(eventId))
                .fetch(r -> new FetchCollectionCompetitorDTO(
                        r.get(ec.DOG_ID),
                        r.get(d.NAME),
                        r.get(d.IDENTITY),
                        r.get(d.BREED),
                        r.get(d.OWNER),
                        r.get(d.HANDLER),
                        r.get(d.TEAM),
                        r.get(d.COUNTRY),
                        r.get(ec.POSITION),
                        r.get(ec.VERIFIED),
                        Boolean.TRUE.equals(r.get(ec.NOT_COMPETING)),
                        null,
                        r.get(ec.BIH),
                        true
                ));
    }
}
