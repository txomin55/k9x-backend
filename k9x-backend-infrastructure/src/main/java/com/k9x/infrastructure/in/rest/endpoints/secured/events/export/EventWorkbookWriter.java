package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationExerciseScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationJudgeScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Renders the private event detail as an xlsx workbook with four sheets: Configuration, Judges, Exercises and
 * Competitors. Sheet names and column headers are resolved through the {@link MessageSource} using the request
 * locale; breed, country and discipline values go through the same {@link ReferenceNameResolver} the read
 * endpoints use, so the workbook shows the names the reader sees in the app and not the raw ids.
 */
public class EventWorkbookWriter {

    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String MULTI_VALUE_SEPARATOR = ", ";

    private final MessageSource messageSource;
    private final ReferenceNameResolver referenceNames;

    public EventWorkbookWriter(MessageSource messageSource, ReferenceNameResolver referenceNames) {
        this.messageSource = messageSource;
        this.referenceNames = referenceNames;
    }

    /**
     * Written with fastexcel rather than Apache POI: the export only needs sheets, bold headers and column
     * widths, and POI brought ~16 MB of classes that stayed in the metaspace after the first export. fastexcel
     * streams the sheets and still packages the strings in {@code xl/sharedStrings.xml}, which Excel and
     * LibreOffice expect. Its zip is then repacked, see {@link #repack}.
     */
    public byte[] write(FetchEventDetailDTO event) {
        return write(event, null, Map.of());
    }

    /**
     * @param classification     when non-null, appends the Classification sheet plus one sheet per competitor.
     * @param coefByExerciseId   the configuration's exercise coefficients, used to show the judges' average
     *                           next to the weighted points. Missing entries simply leave both cells empty.
     */
    public byte[] write(FetchEventDetailDTO event, FetchClassificationDTO classification,
                        Map<String, BigDecimal> coefByExerciseId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new Workbook(out, "k9x", "1.0")) {
            Sheets sheets = new Sheets(workbook);
            writeConfiguration(sheets, event);
            writeJudges(sheets, event.judges());
            writeExercises(sheets, event.exercises());
            writeCompetitors(sheets, event.competitors());

            List<FetchClassificationCompetitorDTO> ranked = rankedCompetitors(classification);
            if (!ranked.isEmpty()) {
                // The classification sheet keeps the ranking order; the per-competitor sheets are laid out by
                // competitor number, which is the order an organizer flips through them in.
                writeClassification(sheets, ranked);
                for (FetchClassificationCompetitorDTO competitor : byCompetitorNumber(ranked)) {
                    writeCompetitorDetail(sheets, competitor, event,
                            coefByExerciseId == null ? Map.of() : coefByExerciseId);
                }
            }
            workbook.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return repack(out.toByteArray());
    }

    /**
     * fastexcel zips the workbook through opczip, which closes every entry with a ZIP64 data descriptor. The
     * central directory is right, but LibreOffice 24 refuses to open such a file ("source file could not be
     * loaded") and {@link ZipInputStream} misreads each entry as empty. Rewriting the same entries through
     * {@link ZipOutputStream} gives the classic layout every reader accepts. The zip is read back through its
     * central directory, which {@link ZipFile} only reads from a file, hence the short-lived temporary one; an
     * export is a few hundred kilobytes.
     */
    private static byte[] repack(byte[] opcZip) {
        Path source = null;
        try {
            source = Files.createTempFile("k9x-export-", ".xlsx");
            Files.write(source, opcZip);
            ByteArrayOutputStream out = new ByteArrayOutputStream(opcZip.length);
            try (ZipFile zip = new ZipFile(source.toFile()); ZipOutputStream repacked = new ZipOutputStream(out)) {
                for (ZipEntry entry : Collections.list(zip.entries())) {
                    repacked.putNextEntry(new ZipEntry(entry.getName()));
                    try (InputStream content = zip.getInputStream(entry)) {
                        content.transferTo(repacked);
                    }
                    repacked.closeEntry();
                }
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (source != null) {
                try {
                    Files.deleteIfExists(source);
                } catch (IOException ignored) {
                    // The OS reclaims its temporary directory; a leftover file must not fail the export.
                }
            }
        }
    }

    private List<FetchClassificationCompetitorDTO> rankedCompetitors(FetchClassificationDTO classification) {
        if (classification == null || classification.obdx() == null
                || classification.obdx().competitors() == null) {
            return List.of();
        }
        return classification.obdx().competitors();
    }

    /** Competitor numbers are nullable; those competitors keep their ranking order at the end. */
    private List<FetchClassificationCompetitorDTO> byCompetitorNumber(
            List<FetchClassificationCompetitorDTO> competitors) {
        return competitors.stream()
                .sorted(Comparator.comparing(FetchClassificationCompetitorDTO::competitorNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * The classification only carries the scores, so the enrollment row — which holds the dog's registry data
     * (origin, license) and the primer — is joined back from the event detail.
     */
    private Map<String, FetchObdxEventCompetitorDTO> enrolledByDogIdentification(FetchEventDetailDTO event) {
        return event.competitors().stream()
                .filter(c -> c.dogIdentification() != null)
                .collect(Collectors.toMap(FetchObdxEventCompetitorDTO::dogIdentification, c -> c, (a, _) -> a));
    }

    private void writeClassification(Sheets sheets, List<FetchClassificationCompetitorDTO> competitors) {
        Sheet sheet = sheets.create(translate("export.sheet.classification"));
        sheet.header(List.of(
                translate("export.column.position"), translate("export.column.competitor_number"),
                translate("export.column.identification"), translate("export.column.dog_name"),
                translate("export.column.handler"), translate("export.column.team"),
                translate("export.column.score"), translate("export.column.percentage"),
                translate("export.column.qualification")));

        int row = 1;
        for (FetchClassificationCompetitorDTO competitor : competitors) {
            sheet.number(row, 0, competitor.position());
            sheet.number(row, 1, competitor.competitorNumber());
            sheet.text(row, 2, competitor.dogIdentification());
            sheet.text(row, 3, competitor.dogName());
            sheet.text(row, 4, competitor.handler());
            sheet.text(row, 5, competitor.team());
            sheet.number(row, 6, competitor.totalScore());
            sheet.number(row, 7, competitor.scoreRating());
            sheet.text(row, 8, competitor.qualification());
            row++;
        }
        sheet.fitColumns();
    }

    /**
     * One sheet per competitor: their full record on top, then a row per exercise with one column per judge,
     * closed by the totals. Sheets are named after the competitor number, which is what an organizer looks
     * for on paper.
     */
    private void writeCompetitorDetail(Sheets sheets, FetchClassificationCompetitorDTO competitor,
                                       FetchEventDetailDTO event, Map<String, BigDecimal> coefByExerciseId) {
        Sheet sheet = sheets.create(competitorSheetName(competitor));

        Map<String, String> exerciseNames = event.exercises().stream()
                .filter(e -> e.id() != null && e.name() != null)
                .collect(Collectors.toMap(FetchEventExerciseDTO::id, FetchEventExerciseDTO::name, (a, _) -> a));
        FetchObdxEventCompetitorDTO enrolled = enrolledByDogIdentification(event).get(competitor.dogIdentification());

        int rowIndex = 0;
        rowIndex = detailRow(sheet, rowIndex, "export.column.competitor_number", competitor.competitorNumber());
        rowIndex = detailRow(sheet, rowIndex, "export.column.start_number", competitor.startOrder());
        rowIndex = detailRow(sheet, rowIndex, "export.column.position", competitor.position());
        rowIndex = detailRow(sheet, rowIndex, "export.column.identification", competitor.dogIdentification());
        rowIndex = detailRow(sheet, rowIndex, "export.column.origin", enrolled == null ? null : enrolled.dogOrigin());
        rowIndex = detailRow(sheet, rowIndex, "export.column.license", enrolled == null ? null : enrolled.dogLicense());
        rowIndex = detailRow(sheet, rowIndex, "export.column.dog_name", competitor.dogName());
        rowIndex = detailRow(sheet, rowIndex, "export.column.breed", referenceNames.breedName(competitor.breed()));
        rowIndex = detailRow(sheet, rowIndex, "export.column.handler", competitor.handler());
        rowIndex = detailRow(sheet, rowIndex, "export.column.team", competitor.team());
        rowIndex = detailRow(sheet, rowIndex, "export.column.country",
                referenceNames.countryName(competitor.country()));
        rowIndex = detailRow(sheet, rowIndex, "export.column.bih", competitor.bih());
        rowIndex = detailRow(sheet, rowIndex, "export.column.primer", enrolled == null ? null : enrolled.primer());
        rowIndex = detailRow(sheet, rowIndex, "export.column.reserve", competitor.reserve());
        rowIndex++;

        // One row per exercise, one column per judge: repeating the exercise name once per judge made the
        // sheet four times taller and hid the comparison between judges, which is the point of scoring.
        List<FetchObdxEventJudgeDTO> judges = event.judges() == null ? List.of() : event.judges();
        List<String> headers = new ArrayList<>();
        headers.add(translate("export.column.order"));
        headers.add(translate("export.column.exercise"));
        headers.add(translate("export.column.tags"));
        judges.forEach(judge -> headers.add(judge.judgeName()));
        headers.add(translate("export.column.average"));
        headers.add(translate("export.column.coefficient"));
        headers.add(translate("export.column.points"));

        sheet.header(rowIndex++, headers);

        int firstJudgeColumn = 3;
        int averageColumn = firstJudgeColumn + judges.size();
        for (FetchClassificationExerciseScoreDTO exercise : orderedExercises(competitor)) {
            int row = rowIndex++;
            sheet.number(row, 0, exercise.exercisePosition());
            sheet.text(row, 1, exerciseNames.get(exercise.exerciseId()));
            sheet.text(row, 2, join(exercise.tags()));

            Map<String, BigDecimal> scoreByJudge = (exercise.judgeScores() == null ? List.<FetchClassificationJudgeScoreDTO>of()
                    : exercise.judgeScores()).stream()
                    .filter(s -> s.judgeId() != null && s.score() != null)
                    .collect(Collectors.toMap(FetchClassificationJudgeScoreDTO::judgeId,
                            FetchClassificationJudgeScoreDTO::score, (a, _) -> a));
            for (int i = 0; i < judges.size(); i++) {
                sheet.number(row, firstJudgeColumn + i, scoreByJudge.get(judges.get(i).judgeId()));
            }

            // Mind the record's field names: exerciseScore() is the maximum attainable for the exercise
            // (max allowed score x coefficient), while totalScore() is what this competitor actually scored.
            sheet.number(row, averageColumn, average(exercise.judgeScores()));
            sheet.number(row, averageColumn + 1, coefByExerciseId.get(exercise.exerciseId()));
            sheet.number(row, averageColumn + 2, exercise.totalScore());
        }

        rowIndex++;
        rowIndex = totalRow(sheet, rowIndex, "export.column.total_score", competitor.totalScore());
        rowIndex = totalRow(sheet, rowIndex, "export.column.percentage", competitor.scoreRating());
        totalRow(sheet, rowIndex, "export.column.qualification", competitor.qualification());

        sheet.fitColumns();
    }

    /**
     * The judges' average for an exercise. Averages only the scores flagged as applying, which is what the
     * domain already resolved: under MID_AVG the extreme scores are excluded, and they must not sway this
     * column either. Deriving it from the weighted points instead would be wrong whenever a yellow card
     * subtracted its flat penalty.
     */
    private BigDecimal average(List<FetchClassificationJudgeScoreDTO> judgeScores) {
        List<BigDecimal> applying = (judgeScores == null ? List.<FetchClassificationJudgeScoreDTO>of() : judgeScores)
                .stream()
                .filter(s -> s.applies() && s.score() != null)
                .map(FetchClassificationJudgeScoreDTO::score)
                .toList();
        if (applying.isEmpty()) {
            return null;
        }
        return applying.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(applying.size()), 2, RoundingMode.HALF_UP);
    }

    private List<FetchClassificationExerciseScoreDTO> orderedExercises(FetchClassificationCompetitorDTO competitor) {
        if (competitor.exercises() == null) {
            return List.of();
        }
        return competitor.exercises().stream()
                .sorted(Comparator.comparingInt(FetchClassificationExerciseScoreDTO::exercisePosition))
                .toList();
    }


    private int detailRow(Sheet sheet, int rowIndex, String labelKey, Object value) {
        sheet.text(rowIndex, 0, translate(labelKey));
        switch (value) {
            case null -> { }
            case Number number -> sheet.number(rowIndex, 1, number);
            case Boolean flag -> sheet.bool(rowIndex, 1, flag);
            default -> sheet.text(rowIndex, 1, value.toString());
        }
        return rowIndex + 1;
    }

    private int totalRow(Sheet sheet, int rowIndex, String labelKey, Object value) {
        sheet.boldText(rowIndex, 0, translate(labelKey));
        switch (value) {
            case null -> { }
            case Number number -> sheet.number(rowIndex, 1, number);
            default -> sheet.text(rowIndex, 1, value.toString());
        }
        return rowIndex + 1;
    }

    /**
     * Competitor numbers are the natural sheet name but are nullable and not guaranteed unique across an
     * event, so the name falls back to the dog; {@link Sheets#create} suffixes it until it is unique.
     */
    private String competitorSheetName(FetchClassificationCompetitorDTO competitor) {
        String base = competitor.competitorNumber() != null
                ? String.valueOf(competitor.competitorNumber())
                : competitor.dogName() != null ? competitor.dogName() : competitor.dogIdentification();
        return base == null ? "-" : base;
    }

    private void writeConfiguration(Sheets sheets, FetchEventDetailDTO event) {
        Sheet sheet = sheets.create(translate("export.sheet.configuration"));
        sheet.header(List.of(
                translate("export.column.field"), translate("export.column.id"), translate("export.column.name")));

        FetchObdxEventDTO obdx = event.obdx();
        FetchEventConfigurationDTO configuration = event.configuration();

        int rowIndex = 1;
        rowIndex = writeConfigurationRow(sheet, rowIndex, "export.field.event",
                obdx == null ? null : obdx.id(), obdx == null ? null : obdx.name());
        rowIndex = writeConfigurationRow(sheet, rowIndex, "export.field.discipline",
                obdx == null ? null : obdx.discipline(),
                referenceNames.disciplineName(obdx == null ? null : obdx.discipline()));
        rowIndex = writeConfigurationRow(sheet, rowIndex, "export.field.federation",
                configuration == null || configuration.federation() == null ? null : configuration.federation().id(),
                configuration == null || configuration.federation() == null ? null : configuration.federation().name());
        rowIndex = writeConfigurationRow(sheet, rowIndex, "export.field.configuration",
                configuration == null ? null : configuration.id(), configuration == null ? null : configuration.name());
        // The enrollment deadline is evaluated by UTC day, so it is written as UTC text rather than as a date cell,
        // which Excel would reinterpret in the reader's own timezone.
        writeConfigurationRow(sheet, rowIndex, "export.field.enrollment_deadline",
                null, formatDeadline(obdx == null ? null : obdx.enrollmentDeadline()));

        sheet.fitColumns();
    }

    private int writeConfigurationRow(Sheet sheet, int rowIndex, String labelKey, String id, String name) {
        sheet.text(rowIndex, 0, translate(labelKey));
        sheet.text(rowIndex, 1, id);
        sheet.text(rowIndex, 2, name);
        return rowIndex + 1;
    }

    private void writeJudges(Sheets sheets, List<FetchObdxEventJudgeDTO> judges) {
        Sheet sheet = sheets.create(translate("export.sheet.judges"));
        sheet.header(List.of(
                translate("export.column.judge_id"), translate("export.column.judge"),
                translate("export.column.collector_email")));

        int row = 1;
        for (FetchObdxEventJudgeDTO judge : judges) {
            sheet.text(row, 0, judge.judgeId());
            sheet.text(row, 1, judge.judgeName());
            sheet.text(row, 2, judge.collectorEmail());
            row++;
        }
        sheet.fitColumns();
    }

    private void writeExercises(Sheets sheets, List<FetchEventExerciseDTO> exercises) {
        Sheet sheet = sheets.create(translate("export.sheet.exercises"));
        sheet.header(List.of(
                translate("export.column.exercise_id"), translate("export.column.exercise"),
                translate("export.column.order"), translate("export.column.judge_ids"),
                translate("export.column.judges"), translate("export.column.tags")));

        int row = 1;
        for (FetchEventExerciseDTO exercise : exercises) {
            sheet.text(row, 0, exercise.id());
            sheet.text(row, 1, exercise.name());
            sheet.number(row, 2, exercise.position());
            List<FetchObdxEventJudgeDTO> judges = exercise.judges() == null ? List.of() : exercise.judges();
            sheet.text(row, 3, join(judges.stream().map(FetchObdxEventJudgeDTO::judgeId).toList()));
            sheet.text(row, 4, join(judges.stream().map(FetchObdxEventJudgeDTO::judgeName).toList()));
            sheet.text(row, 5, join(exercise.tags()));
            row++;
        }
        sheet.fitColumns();
    }

    private void writeCompetitors(Sheets sheets, List<FetchObdxEventCompetitorDTO> competitors) {
        Sheet sheet = sheets.create(translate("export.sheet.competitors"));
        sheet.header(List.of(
                translate("export.column.competitor_number"), translate("export.column.start_number"),
                translate("export.column.identification"), translate("export.column.origin"),
                translate("export.column.license"),
                translate("export.column.dog_name"), translate("export.column.handler"),
                translate("export.column.reserve"), translate("export.column.sex"),
                translate("export.column.bih"), translate("export.column.primer"),
                translate("export.column.country"), translate("export.column.team")));

        int row = 1;
        for (FetchObdxEventCompetitorDTO competitor : competitors) {
            sheet.number(row, 0, competitor.competitorNumber());
            sheet.number(row, 1, competitor.startNumber());
            sheet.text(row, 2, competitor.dogIdentification());
            sheet.text(row, 3, competitor.dogOrigin());
            sheet.text(row, 4, competitor.dogLicense());
            sheet.text(row, 5, competitor.dogName());
            sheet.text(row, 6, competitor.handler());
            sheet.bool(row, 7, competitor.reserve());
            sheet.text(row, 8, competitor.sex());
            sheet.bool(row, 9, competitor.bih());
            sheet.text(row, 10, competitor.primer());
            sheet.text(row, 11, referenceNames.countryName(competitor.country()));
            sheet.text(row, 12, competitor.team());
            row++;
        }
        sheet.fitColumns();
    }

    private String join(List<String> values) {
        return values == null ? null : String.join(MULTI_VALUE_SEPARATOR, values);
    }

    private String formatDeadline(Long enrollmentDeadline) {
        if (enrollmentDeadline == null) {
            return null;
        }
        return DEADLINE_FORMAT.format(Instant.ofEpochMilli(enrollmentDeadline).atZone(ZoneOffset.UTC));
    }

    private String translate(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    /**
     * The workbook's sheets, named the way Excel accepts them. Excel caps a sheet name at 31 characters, bans
     * {@code : \ / ? * [ ]} and a leading or trailing apostrophe, and compares names ignoring case, so a
     * clash gets a " (n)" suffix instead of failing the export.
     */
    private final class Sheets {

        private static final int MAX_SHEET_NAME = 31;

        private final Workbook workbook;
        private final Set<String> taken = new HashSet<>();

        private Sheets(Workbook workbook) {
            this.workbook = workbook;
        }

        Sheet create(String name) {
            String base = safeName(name);
            String candidate = base;
            for (int suffix = 2; !taken.add(candidate.toLowerCase(Locale.ROOT)); suffix++) {
                candidate = safeName(base + " (" + suffix + ")");
            }
            return new Sheet(workbook.newWorksheet(candidate));
        }

        private static String safeName(String name) {
            if (name == null || name.isBlank()) {
                return "-";
            }
            String safe = name.replaceAll("[:\\\\/?*\\[\\]]", " ");
            if (safe.startsWith("'")) {
                safe = " " + safe.substring(1);
            }
            if (safe.length() > MAX_SHEET_NAME) {
                safe = safe.substring(0, MAX_SHEET_NAME);
            }
            if (safe.endsWith("'")) {
                safe = safe.substring(0, safe.length() - 1) + " ";
            }
            return safe;
        }
    }

    /**
     * One sheet being written. Empty values leave the cell empty, and it remembers the widest value of each
     * column so {@link #fitColumns} can size them to their content, as a reader would by double-clicking.
     */
    private final class Sheet {

        /** Excel's ceiling for a column width, in characters. */
        private static final int MAX_COLUMN_WIDTH = 255;
        /** Room for the cell padding and the bold headers, which render wider than their character count. */
        private static final int WIDTH_PADDING = 2;

        private final Worksheet worksheet;
        private final Map<Integer, Integer> widestByColumn = new HashMap<>();

        private Sheet(Worksheet worksheet) {
            this.worksheet = worksheet;
        }

        void header(List<String> headers) {
            header(0, headers);
        }

        void header(int row, List<String> headers) {
            for (int column = 0; column < headers.size(); column++) {
                boldText(row, column, headers.get(column));
            }
        }

        void text(int row, int column, String value) {
            if (value != null) {
                worksheet.value(row, column, value);
                measure(column, value);
            }
        }

        void boldText(int row, int column, String value) {
            if (value != null) {
                text(row, column, value);
                worksheet.style(row, column).bold().set();
            }
        }

        void number(int row, int column, Number value) {
            if (value != null) {
                worksheet.value(row, column, value.doubleValue());
                measure(column, value.toString());
            }
        }

        /**
         * Booleans are written as the localised "yes"/"no" rather than as Excel TRUE/FALSE cells: the workbook
         * is read by people, and Excel renders a boolean cell in its own UI language, not the requested one.
         */
        void bool(int row, int column, Boolean value) {
            if (value != null) {
                text(row, column, translate(value ? "export.value.yes" : "export.value.no"));
            }
        }

        void fitColumns() {
            widestByColumn.forEach((column, widest) ->
                    worksheet.width(column, Math.min(MAX_COLUMN_WIDTH, widest + WIDTH_PADDING)));
        }

        private void measure(int column, String value) {
            widestByColumn.merge(column, value.length(), Math::max);
        }
    }
}
