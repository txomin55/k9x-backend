package com.k9x.domain.disciplines.obdx;

/**
 * The OBDX ranking policy shared by the classification's ordering and tie handling.
 *
 * <p>Competitors are grouped into tiers before any score comparison: regular competitors rank first, then
 * red-carded (disqualified) competitors — ordered by score among themselves — and finally the not-competing
 * ones. Within a tier the higher total score wins, with configured tie-break exercises resolving the rest.
 */
public final class ObdxRanking {

    /** Primary sort key: regular competitors first. */
    public static final int REGULAR_TIER = 0;
    /** Red-carded (disqualified) competitors rank below regular ones. */
    public static final int RED_CARD_TIER = 1;
    /** Not-competing competitors rank last. */
    public static final int NOT_COMPETING_TIER = 2;

    private ObdxRanking() {
    }

    /** The ranking tier used as the primary sort key. */
    public static int tier(boolean notCompeting, boolean hasRedCard) {
        if (notCompeting) {
            return NOT_COMPETING_TIER;
        }
        if (hasRedCard) {
            return RED_CARD_TIER;
        }
        return REGULAR_TIER;
    }
}
