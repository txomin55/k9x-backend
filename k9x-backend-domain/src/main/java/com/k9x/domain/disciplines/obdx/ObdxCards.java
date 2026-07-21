package com.k9x.domain.disciplines.obdx;

/**
 * The OBDX disciplinary-card rule: a competitor that accumulates a second yellow card, or that holds a red
 * card, is disqualified — its participation is over and it can no longer receive scores.
 */
public final class ObdxCards {

    /** A competitor is disqualified once it reaches this many yellow cards. */
    public static final int DISQUALIFYING_YELLOW_CARDS = 2;

    private ObdxCards() {
    }

    /** Whether the card tally disqualifies the competitor. */
    public static boolean isDisqualified(long yellowCardCount, boolean hasRedCard) {
        return yellowCardCount >= DISQUALIFYING_YELLOW_CARDS || hasRedCard;
    }
}
