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

        return dsl.select(ec.DOG_IDENTIFICATION, ec.START_NUMBER, ec.COMPETITOR_NUMBER, ec.VERIFIED, ec.NOT_COMPETING, ec.BIH, ec.PRIMER, ec.RESERVE,
                        d.NAME, d.ORIGIN, d.BREED, d.OWNER, d.HANDLER, d.TEAM, d.COUNTRY,
                        ec.HANDLER, ec.TEAM, ec.COUNTRY)
                .from(ec)
                .join(d).on(d.IDENTIFICATION.eq(ec.DOG_IDENTIFICATION).and(d.DELETED_AT.isNull()))
                .where(ec.EVENT_ID.eq(eventId))
                .orderBy(ec.START_NUMBER.asc().nullsLast(), ec.DOG_IDENTIFICATION.asc())
                .fetch(r -> new FetchCollectionCompetitorDTO(
                        r.get(ec.DOG_IDENTIFICATION),
                        r.get(d.NAME),
                        r.get(d.ORIGIN),
                        r.get(d.BREED),
                        r.get(d.OWNER),
                        firstNonBlank(r.get(ec.HANDLER), r.get(d.HANDLER)),
                        firstNonBlank(r.get(ec.TEAM), r.get(d.TEAM)),
                        firstNonBlank(r.get(ec.COUNTRY), r.get(d.COUNTRY)),
                        r.get(ec.START_NUMBER),
                        r.get(ec.COMPETITOR_NUMBER),
                        r.get(ec.VERIFIED),
                        Boolean.TRUE.equals(r.get(ec.NOT_COMPETING)),
                        null,
                        r.get(ec.BIH),
                        r.get(ec.PRIMER),
                        r.get(ec.RESERVE),
                        true
                ));
    }

    private static String firstNonBlank(String snapshot, String current) {
        return snapshot == null || snapshot.isBlank() ? current : snapshot;
    }
}
