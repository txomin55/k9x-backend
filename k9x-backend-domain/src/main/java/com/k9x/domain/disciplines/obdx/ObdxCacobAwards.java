package com.k9x.domain.disciplines.obdx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The CACOB / CACIOB award policy (and their reserve RCACOB / RCACIOB) for an OBDX event that enables them.
 *
 * <p>A competitor <em>qualifies</em> when its dog has three FCI generations confirmed and its total-score
 * rating is above {@link #MIN_SCORE_RATING}. The main award only goes to the overall winner (ranking
 * position 1) if it qualifies — no substitute winner is promoted when it doesn't. The reserve award goes to the
 * next qualifying competitor walking down the ranking, skipping the main award's recipient if there was one;
 * when the winner didn't qualify (so no main award was granted), the reserve simply goes to the first
 * qualifying competitor in the ranking, whatever its position. CACOB and CACIOB are resolved independently, so
 * a competitor may earn both.
 */
public final class ObdxCacobAwards {

    public static final String CACOB = "CACOB";
    public static final String CACIOB = "CACIOB";
    public static final String RESERVE_CACOB = "RCACOB";
    public static final String RESERVE_CACIOB = "RCACIOB";

    /** A competitor must exceed this total-score rating to qualify for a CACOB/CACIOB award. */
    public static final BigDecimal MIN_SCORE_RATING = new BigDecimal("80");

    private ObdxCacobAwards() {
    }

    /**
     * One competitor in ranking order.
     *
     * @param fciConfirmed whether the dog has three FCI generations confirmed.
     * @param scoreRating  the competitor's total-score rating (0–100), or {@code null} when it has no score.
     * @param position     the competitor's ranking position (1 = overall winner).
     */
    public record Candidate(boolean fciConfirmed, BigDecimal scoreRating, int position) {
    }

    /**
     * Computes, for the given competitors <em>in ranking order</em>, which award codes each one earns. The
     * returned map is keyed by the competitor's index within {@code rankingOrder} and only contains entries for
     * competitors that earn at least one award.
     */
    public static Map<Integer, List<String>> assign(List<Candidate> rankingOrder, List<String> eventAwards) {
        Map<Integer, List<String>> awardsByIndex = new LinkedHashMap<>();
        if (eventAwards == null) {
            return awardsByIndex;
        }
        if (eventAwards.contains(CACOB)) {
            assignPair(rankingOrder, CACOB, RESERVE_CACOB, awardsByIndex);
        }
        if (eventAwards.contains(CACIOB)) {
            assignPair(rankingOrder, CACIOB, RESERVE_CACIOB, awardsByIndex);
        }
        return awardsByIndex;
    }

    private static void assignPair(List<Candidate> rankingOrder, String mainAward, String reserveAward,
                                   Map<Integer, List<String>> awardsByIndex) {
        List<Integer> qualifyingIndexes = new ArrayList<>();
        for (int i = 0; i < rankingOrder.size(); i++) {
            if (qualifies(rankingOrder.get(i))) {
                qualifyingIndexes.add(i);
            }
        }
        if (qualifyingIndexes.isEmpty()) {
            return;
        }

        int reserveCandidate = 0;
        int firstIndex = qualifyingIndexes.get(0);
        if (rankingOrder.get(firstIndex).position() == 1) {
            add(awardsByIndex, firstIndex, mainAward);
            reserveCandidate = 1;
        }
        if (reserveCandidate < qualifyingIndexes.size()) {
            add(awardsByIndex, qualifyingIndexes.get(reserveCandidate), reserveAward);
        }
    }

    private static boolean qualifies(Candidate candidate) {
        return candidate.fciConfirmed()
                && candidate.scoreRating() != null
                && candidate.scoreRating().compareTo(MIN_SCORE_RATING) > 0;
    }

    private static void add(Map<Integer, List<String>> awardsByIndex, int index, String award) {
        awardsByIndex.computeIfAbsent(index, _ -> new ArrayList<>()).add(award);
    }
}
