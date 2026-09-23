package com.k9x.application.methodology.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.methodology.port.GetMethodologyCatalogPort;
import com.k9x.application.methodology.use_case.dto.MethodologyCatalogDTO;
import com.k9x.application.methodology.use_case.dto.ObdxMethodologyDTO;
import com.k9x.domain.disciplines.obdx.ObdxCompetitorEventScore;
import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.disciplines.obdx.ObdxScoreRating;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The OBDX methodology page as data: the scale, tiers, categories, every configuration's band and the merit curve,
 * all read off the domain rules that compute the rank ({@link ObdxRank}, {@link ObdxConfigurationsRankThresholds},
 * {@link ObdxCompetitorEventScore}), so the page cannot drift from the real calculation. Only the names come from
 * the catalogue. Public: it asserts nothing about the caller.
 */
public class GetObdxMethodologyServiceCase {

    /** Contract version of {@code GET /obdx/methodology}; bumped by any change that breaks a consumer. */
    public static final int SCHEMA_VERSION = 2;

    /** The federation the page opens on, so it goes first. */
    private static final String FIRST_FEDERATION = "FCI";

    /** The merit curve is drawn for one example event: the FCI grade 3 world final. */
    private static final ObdxConfigurationsRankThresholds MERIT_EXAMPLE_BAND = ObdxConfigurationsRankThresholds.FCI_GRADE_3;
    private static final ObdxEventCategory MERIT_EXAMPLE_CATEGORY = ObdxEventCategory.WC_FINAL;

    /** The merit curve's x axis runs in tenths of the maximum score, and the floor starts one tenth below the first qualification. */
    private static final BigDecimal X_STEPS = BigDecimal.TEN;

    private final GetMethodologyCatalogPort getMethodologyCatalogPort;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;

    public GetObdxMethodologyServiceCase(GetMethodologyCatalogPort getMethodologyCatalogPort,
                                         GetObdxClassificationConfigPort getObdxClassificationConfigPort) {
        this.getMethodologyCatalogPort = getMethodologyCatalogPort;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
    }

    public ObdxMethodologyDTO getMethodology() {
        MethodologyCatalogDTO catalog = getMethodologyCatalogPort.getCatalog();
        return new ObdxMethodologyDTO(SCHEMA_VERSION, globalScale(), tiers(), categories(catalog),
                federations(catalog), meritCurve(catalog));
    }

    private static ObdxMethodologyDTO.GlobalScale globalScale() {
        return new ObdxMethodologyDTO.GlobalScale(ObdxRank.SCALE_MIN, ObdxRank.SCALE_MAX,
                Arrays.stream(ObdxRank.values())
                        .map(rank -> new ObdxMethodologyDTO.ScaleRange(rank.name(), rank.minScore(), rank.maxScore()))
                        .toList());
    }

    private static List<ObdxMethodologyDTO.Tier> tiers() {
        return ObdxConfigurationsRankThresholds.tiers().stream()
                .map(tier -> new ObdxMethodologyDTO.Tier(tier.tier(), competitors(tier)))
                .toList();
    }

    private static ObdxMethodologyDTO.CompetitorRange competitors(ObdxConfigurationsRankThresholds.Tier tier) {
        return new ObdxMethodologyDTO.CompetitorRange(tier.minCompetitors(), tier.maxCompetitors());
    }

    private static List<ObdxMethodologyDTO.Category> categories(MethodologyCatalogDTO catalog) {
        return Arrays.stream(ObdxEventCategory.values())
                .map(category -> new ObdxMethodologyDTO.Category(category.name(),
                        catalog.categoryNames().get(category), isChampionship(category)))
                .toList();
    }

    /** A championship round is a category some configuration refuses: only the grade hosting it accepts it. */
    private static boolean isChampionship(ObdxEventCategory category) {
        return Arrays.stream(ObdxConfigurationsRankThresholds.values()).anyMatch(band -> !band.allows(category));
    }

    private static List<ObdxMethodologyDTO.Federation> federations(MethodologyCatalogDTO catalog) {
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, List<ObdxMethodologyDTO.Grade>> grades = new LinkedHashMap<>();
        for (MethodologyCatalogDTO.Configuration configuration : catalog.configurations()) {
            ObdxConfigurationsRankThresholds band =
                    ObdxConfigurationsRankThresholds.fromConfigurationId(configuration.configurationId());
            if (band == null) {
                continue;
            }
            names.putIfAbsent(configuration.federationId(), configuration.federationName());
            grades.computeIfAbsent(configuration.federationId(), _ -> new ArrayList<>()).add(grade(band, configuration));
        }
        return names.entrySet().stream()
                .sorted(Comparator.comparing(entry -> !FIRST_FEDERATION.equals(entry.getKey())))
                .map(entry -> new ObdxMethodologyDTO.Federation(entry.getKey(), entry.getValue(),
                        grades.get(entry.getKey()).stream()
                                .sorted(Comparator.comparingInt(grade -> grade.band().min()))
                                .toList()))
                .toList();
    }

    private static ObdxMethodologyDTO.Grade grade(ObdxConfigurationsRankThresholds band,
                                                  MethodologyCatalogDTO.Configuration configuration) {
        List<ObdxMethodologyDTO.GradeCategory> categories = Arrays.stream(ObdxEventCategory.values())
                .filter(band::allows)
                .map(category -> gradeCategory(band, category))
                .sorted(Comparator.comparingInt(category -> category.subBand().min()))
                .toList();
        return new ObdxMethodologyDTO.Grade(gradeId(band), configuration.name(),
                new ObdxMethodologyDTO.ScoreRange(band.min(), band.max()), possibleLetters(band), categories);
    }

    /** The configuration id without its version and with dots as underscores, e.g. {@code OBDX_FCI_GRADE_3}. */
    static String gradeId(ObdxConfigurationsRankThresholds band) {
        return band.configurationKey().replace('.', '_');
    }

    private static List<String> possibleLetters(ObdxConfigurationsRankThresholds band) {
        int lowest = ObdxRank.fromScore(band.min()).ordinal();
        int highest = ObdxRank.fromScore(band.max()).ordinal();
        return Arrays.stream(ObdxRank.values())
                .filter(rank -> rank.ordinal() >= lowest && rank.ordinal() <= highest)
                .map(ObdxRank::name)
                .toList();
    }

    private static ObdxMethodologyDTO.GradeCategory gradeCategory(ObdxConfigurationsRankThresholds band,
                                                                  ObdxEventCategory category) {
        ObdxConfigurationsRankThresholds.Band subBand = band.subBand(category);
        List<ObdxMethodologyDTO.GradeCategoryTier> tiers = ObdxConfigurationsRankThresholds.tiers().stream()
                .map(tier -> {
                    int rankScore = band.eventScore(tier.minCompetitors(), category);
                    return new ObdxMethodologyDTO.GradeCategoryTier(tier.tier(), competitors(tier), rankScore,
                            ObdxRank.labelFromScore(rankScore));
                })
                .toList();
        return new ObdxMethodologyDTO.GradeCategory(category.name(),
                new ObdxMethodologyDTO.ScoreRange(subBand.min(), subBand.max()), subBand.min() == subBand.max(), tiers);
    }

    private ObdxMethodologyDTO.MeritCurve meritCurve(MethodologyCatalogDTO catalog) {
        ObdxClassificationConfigDTO config = getObdxClassificationConfigPort.getConfig(
                catalog.configurationOf(MERIT_EXAMPLE_BAND).configurationId());
        BigDecimal maxScore = ObdxScoreRating.maxPossibleTotal(config.maxAllowedScore(), config.coefByExerciseId(),
                config.coefByExerciseId().keySet());
        List<ObdxClassificationConfigDTO.QualificationThreshold> qualifications = config.qualifications().stream()
                .sorted(Comparator.comparing(ObdxClassificationConfigDTO.QualificationThreshold::minScore))
                .toList();
        BigDecimal firstQualification = qualifications.getFirst().minScore();
        BigDecimal topQualification = qualifications.getLast().minScore();

        int eventScore = MERIT_EXAMPLE_BAND.eventScore(
                ObdxConfigurationsRankThresholds.tiers().getLast().minCompetitors(), MERIT_EXAMPLE_CATEGORY);
        int gradeFloor = MERIT_EXAMPLE_BAND.min();
        MeritPoint point = total -> new ObdxMethodologyDTO.Point(total, ObdxCompetitorEventScore.of(
                eventScore, gradeFloor, firstQualification, topQualification, total, maxScore));

        BigDecimal step = maxScore.divide(X_STEPS);
        List<ObdxMethodologyDTO.Point> floor = List.of(
                point.at(firstQualification.subtract(step)), point.at(firstQualification.subtract(BigDecimal.ONE)));
        List<ObdxMethodologyDTO.Point> curve = new ArrayList<>();
        qualifications.forEach(qualification -> curve.add(point.at(qualification.minScore())));
        curve.add(point.at(topQualification.add(maxScore).divide(BigDecimal.TWO)));
        curve.add(point.at(maxScore));

        ObdxMethodologyDTO.MeritContext context = new ObdxMethodologyDTO.MeritContext(
                gradeId(MERIT_EXAMPLE_BAND), MERIT_EXAMPLE_CATEGORY.name(), eventScore, gradeFloor,
                maxScore.intValueExact(),
                qualifications.stream()
                        .map(qualification -> new ObdxMethodologyDTO.Qualification(qualification.id(),
                                catalog.qualificationEnglishNames().getOrDefault(qualification.id(), qualification.id()),
                                qualification.minScore().intValue(), qualification == qualifications.getLast()))
                        .toList(),
                new ObdxMethodologyDTO.MeritParameters(
                        ObdxCompetitorEventScore.QUALIFICATION_UNLOCK_SHARE.movePointRight(2).intValueExact(),
                        ObdxCompetitorEventScore.KNEE_SHARE, gradeFloor - 1));
        return new ObdxMethodologyDTO.MeritCurve(context, List.of(
                new ObdxMethodologyDTO.Series("floor", floor),
                new ObdxMethodologyDTO.Series("curve", curve),
                new ObdxMethodologyDTO.Series("knee", List.of(point.at(topQualification)))));
    }

    /** One point of the merit curve: a competitor total and the event score the domain gives it. */
    @FunctionalInterface
    private interface MeritPoint {
        ObdxMethodologyDTO.Point at(BigDecimal total);
    }
}
