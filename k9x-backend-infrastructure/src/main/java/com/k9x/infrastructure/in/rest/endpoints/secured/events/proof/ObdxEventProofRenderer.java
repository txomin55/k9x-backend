package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import com.k9x.application.events.obdx.exceptions.ObdxCompetitorNotFoundException;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.disciplines.obdx.ObdxConfigurationGrade;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Renders the OBDX working booklet strip.
 *
 * <p>Everything OBDX-shaped about the proof lives here: where the score comes from, how the class number and
 * the qualification are spelled, and how the file is named. The score is read from the classification use
 * case rather than re-aggregated, so the download inherits its cache and daily snapshot.
 */
public class ObdxEventProofRenderer implements EventProofRenderer {

    private static final String DEFAULT_FILE_NAME = "event-proof";
    private static final int SCORE_DECIMALS = 2;

    private final GetEventClassificationServiceCase getEventClassificationServiceCase;
    private final EventProofPdfWriter eventProofPdfWriter;
    private final MessageSource messageSource;

    public ObdxEventProofRenderer(GetEventClassificationServiceCase getEventClassificationServiceCase,
                                  EventProofPdfWriter eventProofPdfWriter, MessageSource messageSource) {
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
        this.eventProofPdfWriter = eventProofPdfWriter;
        this.messageSource = messageSource;
    }

    @Override
    public String discipline() {
        return Discipline.OBDX.name();
    }

    @Override
    public EventProofDocument render(String eventId, String competitorId, FetchEventDetailDTO event) {
        FetchClassificationDTO classification = getEventClassificationServiceCase.getClassification(eventId);
        FetchClassificationCompetitorDTO competitor = findCompetitor(classification, competitorId);
        byte[] pdf = eventProofPdfWriter.write(toProofData(event, competitor));
        return new EventProofDocument(pdf, fileName(event, competitor));
    }

    private FetchClassificationCompetitorDTO findCompetitor(FetchClassificationDTO classification,
                                                            String competitorId) {
        List<FetchClassificationCompetitorDTO> competitors = classification.obdx() == null
                || classification.obdx().competitors() == null
                ? List.of()
                : classification.obdx().competitors();
        return competitors.stream()
                .filter(c -> competitorId.equals(c.dogIdentification()))
                .findFirst()
                .orElseThrow(ObdxCompetitorNotFoundException::new);
    }

    private EventProofData toProofData(FetchEventDetailDTO event, FetchClassificationCompetitorDTO competitor) {
        FetchObdxEventDTO obdx = event.obdx();
        return new EventProofData(
                obdx.name(),
                obdx.stageDateFrom(),
                obdx.organizerName(),
                obdx.address(),
                className(event),
                formatScore(competitor.totalScore()),
                qualification(competitor.qualification()),
                obdx.commissioner(),
                competitor.position(),
                competitor.handler(),
                competitor.dogName(),
                event.judges() == null ? List.of() : event.judges().stream()
                        .map(j -> new EventProofData.Judge(j.judgeName(), j.mainJudge()))
                        .toList());
    }

    /**
     * The booklet's CLASE box takes the bare grade number. Configurations without a numeric grade (debutante
     * and friends) fall back to the configuration's translated name, which is the closest thing to a class
     * they have.
     */
    private String className(FetchEventDetailDTO event) {
        if (event.configuration() == null) {
            return null;
        }
        String grade = ObdxConfigurationGrade.resolve(event.configuration().id());
        return grade != null ? grade : event.configuration().name();
    }

    /** Locale-aware so a Spanish booklet reads 233,25 and an English one 233.25. */
    private String formatScore(BigDecimal totalScore) {
        if (totalScore == null) {
            return null;
        }
        NumberFormat format = NumberFormat.getNumberInstance(LocaleContextHolder.getLocale());
        format.setMinimumFractionDigits(SCORE_DECIMALS);
        format.setMaximumFractionDigits(SCORE_DECIMALS);
        return format.format(totalScore);
    }

    /**
     * Qualifications travel as tier ids (EXC, MB, B, NC, DISQ). The booklet is filled in words, so they are
     * translated here — with the id itself as the fallback, like every other translated label in the exports.
     */
    private String qualification(String qualification) {
        if (qualification == null || qualification.isBlank()) {
            return null;
        }
        String key = "proof.qualification." + qualification.toLowerCase(Locale.ROOT);
        return messageSource.getMessage(key, null, qualification, LocaleContextHolder.getLocale());
    }

    /**
     * Named after the event and the competitor so a folder of downloads stays readable. The endpoint encodes
     * it as UTF-8, which makes Spring emit the RFC 5987 {@code filename*} form so accents survive; only the
     * characters no filesystem accepts are replaced.
     */
    private String fileName(FetchEventDetailDTO event, FetchClassificationCompetitorDTO competitor) {
        String eventName = event.obdx() == null ? null : event.obdx().name();
        String base = eventName == null || eventName.isBlank() ? DEFAULT_FILE_NAME : eventName.trim();
        String competitorName = competitor.dogName() == null || competitor.dogName().isBlank()
                ? competitor.dogIdentification()
                : competitor.dogName().trim();
        return (base + "-" + competitorName).replaceAll("[/\\\\:*?\"<>|\\p{Cntrl}]", "_") + ".pdf";
    }
}
