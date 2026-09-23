package com.k9x.application.methodology.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

/** What {@code GET /obdx/methodology} returns: the OBDX rank rules, laid out for the methodology page. */
public record ObdxMethodologyDTO(int schemaVersion, GlobalScale globalScale, List<Tier> tiers,
                                 List<Category> categories, List<Federation> federations, MeritCurve meritCurve) {

    public record ScoreRange(int min, int max) {
    }

    /** {@code max} is {@code null} on the last tier, which has no upper bound. */
    public record CompetitorRange(int min, Integer max) {
    }

    public record ScaleRange(String letter, int min, int max) {
    }

    public record GlobalScale(int min, int max, List<ScaleRange> ranges) {
    }

    public record Tier(int tier, CompetitorRange competitors) {
    }

    public record Category(String id, LocalizedTextDTO name, boolean championship) {
    }

    public record GradeCategoryTier(int tier, CompetitorRange competitors, int rankScore, String letter) {
    }

    public record GradeCategory(String id, ScoreRange subBand, boolean fixed, List<GradeCategoryTier> tiers) {
    }

    public record Grade(String id, LocalizedTextDTO name, ScoreRange band, List<String> possibleLetters,
                        List<GradeCategory> categories) {
    }

    public record Federation(String id, String name, List<Grade> grades) {
    }

    public record Qualification(String id, String nameEn, int score, boolean top) {
    }

    public record MeritParameters(int unlockPct, BigDecimal kneeShare, int floorBelowFirstQualification) {
    }

    public record MeritContext(String configuration, String category, int eventScore, int gradeFloor, int maxScore,
                               List<Qualification> qualifications, MeritParameters parameters) {
    }

    public record Point(BigDecimal x, BigDecimal y) {
    }

    public record Series(String id, List<Point> points) {
    }

    public record MeritCurve(MeritContext context, List<Series> series) {
    }
}
