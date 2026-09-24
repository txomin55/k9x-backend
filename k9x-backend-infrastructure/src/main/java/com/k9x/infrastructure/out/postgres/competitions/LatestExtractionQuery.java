package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.ExtractionMetadata;
import org.jooq.DSLContext;

/**
 * A competition can be extracted more than once — a re-collection from a better source — so read models show only
 * its most recent extraction: it is the one that describes the data currently loaded.
 */
public final class LatestExtractionQuery {

    private LatestExtractionQuery() {
    }

    /** The competition's latest extraction, or {@code null} when it has none (e.g. created through the app). */
    public static CompetitionExtraction of(DSLContext dsl, String competitionId) {
        ExtractionMetadata em = Tables.EXTRACTION_METADATA;
        return dsl.select(em.EXTRACTION_ID, em.SOURCE, em.EXTRACTION_TIMESTAMP, em.TYPE, em.RESTRICTED, em.CREATED_AT)
                .from(em)
                .where(em.COMPETITION_ID.eq(competitionId))
                .orderBy(em.EXTRACTION_TIMESTAMP.desc())
                .limit(1)
                .fetchOptional(r -> new CompetitionExtraction(r.get(em.EXTRACTION_ID), r.get(em.SOURCE),
                        r.get(em.EXTRACTION_TIMESTAMP), r.get(em.TYPE), Boolean.TRUE.equals(r.get(em.RESTRICTED)),
                        r.get(em.CREATED_AT)))
                .orElse(null);
    }
}
