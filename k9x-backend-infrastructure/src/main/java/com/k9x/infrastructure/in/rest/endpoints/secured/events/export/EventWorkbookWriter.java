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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Uses the in-memory {@link XSSFWorkbook} rather than the streaming SXSSF variant on purpose: SXSSF
     * stores every string inline yet still emits (and declares) an empty {@code xl/sharedStrings.xml}, and
     * both Excel and LibreOffice reject the resulting workbook — Excel offers to "repair" it and rewrites
     * cells while doing so. An event holds a few hundred rows at most, so streaming buys nothing here.
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
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            writeConfiguration(workbook, headerStyle, event);
            writeJudges(workbook, headerStyle, event.judges());
            writeExercises(workbook, headerStyle, event.exercises());
            writeCompetitors(workbook, headerStyle, event.competitors());

            List<FetchClassificationCompetitorDTO> ranked = rankedCompetitors(classification);
            if (!ranked.isEmpty()) {
                // The classification sheet keeps the ranking order; the per-competitor sheets are laid out by
                // competitor number, which is the order an organizer flips through them in.
                writeClassification(workbook, headerStyle, ranked);
                for (FetchClassificationCompetitorDTO competitor : byCompetitorNumber(ranked)) {
                    writeCompetitorDetail(workbook, headerStyle, competitor, event,
                            coefByExerciseId == null ? Map.of() : coefByExerciseId);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    private void writeClassification(Workbook workbook, CellStyle headerStyle,
                                     List<FetchClassificationCompetitorDTO> competitors) {
        Sheet sheet = createSheet(workbook, "export.sheet.classification");
        writeHeader(sheet, headerStyle, List.of(
                translate("export.column.position"), translate("export.column.competitor_number"),
                translate("export.column.identification"), translate("export.column.dog_name"),
                translate("export.column.handler"), translate("export.column.team"),
                translate("export.column.score"), translate("export.column.percentage"),
                translate("export.column.qualification")));

        int rowIndex = 1;
        for (FetchClassificationCompetitorDTO competitor : competitors) {
            Row row = sheet.createRow(rowIndex++);
            setNumber(row, 0, competitor.position());
            setNumber(row, 1, competitor.competitorNumber());
            setText(row, 2, competitor.dogIdentification());
            setText(row, 3, competitor.dogName());
            setText(row, 4, competitor.handler());
            setText(row, 5, competitor.team());
            setNumber(row, 6, competitor.totalScore());
            setNumber(row, 7, competitor.scoreRating());
            setText(row, 8, competitor.qualification());
        }
        autoSize(sheet, 9);
    }

    /**
     * One sheet per competitor: their full record on top, then a row per exercise with one column per judge,
     * closed by the totals. Sheets are named after the competitor number, which is what an organizer looks
     * for on paper.
     */
    private void writeCompetitorDetail(Workbook workbook, CellStyle headerStyle,
                                       FetchClassificationCompetitorDTO competitor, FetchEventDetailDTO event,
                                       Map<String, BigDecimal> coefByExerciseId) {
        Sheet sheet = workbook.createSheet(competitorSheetName(workbook, competitor));

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

        Row scoresHeader = sheet.createRow(rowIndex++);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = scoresHeader.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        int firstJudgeColumn = 3;
        int averageColumn = firstJudgeColumn + judges.size();
        for (FetchClassificationExerciseScoreDTO exercise : orderedExercises(competitor)) {
            Row row = sheet.createRow(rowIndex++);
            setNumber(row, 0, exercise.exercisePosition());
            setText(row, 1, exerciseNames.get(exercise.exerciseId()));
            setText(row, 2, join(exercise.tags()));

            Map<String, BigDecimal> scoreByJudge = (exercise.judgeScores() == null ? List.<FetchClassificationJudgeScoreDTO>of()
                    : exercise.judgeScores()).stream()
                    .filter(s -> s.judgeId() != null && s.score() != null)
                    .collect(Collectors.toMap(FetchClassificationJudgeScoreDTO::judgeId,
                            FetchClassificationJudgeScoreDTO::score, (a, _) -> a));
            for (int i = 0; i < judges.size(); i++) {
                setNumber(row, firstJudgeColumn + i, scoreByJudge.get(judges.get(i).judgeId()));
            }

            // Mind the record's field names: exerciseScore() is the maximum attainable for the exercise
            // (max allowed score x coefficient), while totalScore() is what this competitor actually scored.
            setNumber(row, averageColumn, average(exercise.judgeScores()));
            setNumber(row, averageColumn + 1, coefByExerciseId.get(exercise.exerciseId()));
            setNumber(row, averageColumn + 2, exercise.totalScore());
        }

        rowIndex++;
        rowIndex = totalRow(sheet, headerStyle, rowIndex, "export.column.total_score", competitor.totalScore());
        rowIndex = totalRow(sheet, headerStyle, rowIndex, "export.column.percentage", competitor.scoreRating());
        totalRow(sheet, headerStyle, rowIndex, "export.column.qualification", competitor.qualification());

        autoSize(sheet, headers.size());
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
        Row row = sheet.createRow(rowIndex);
        Cell label = row.createCell(0);
        label.setCellValue(translate(labelKey));
        switch (value) {
            case null -> { }
            case Number number -> setNumber(row, 1, number);
            case Boolean flag -> setBoolean(row, 1, flag);
            default -> setText(row, 1, value.toString());
        }
        return rowIndex + 1;
    }

    private int totalRow(Sheet sheet, CellStyle headerStyle, int rowIndex, String labelKey, Object value) {
        Row row = sheet.createRow(rowIndex);
        Cell label = row.createCell(0);
        label.setCellValue(translate(labelKey));
        label.setCellStyle(headerStyle);
        switch (value) {
            case null -> { }
            case Number number -> setNumber(row, 1, number);
            default -> setText(row, 1, value.toString());
        }
        return rowIndex + 1;
    }

    /**
     * Competitor numbers are the natural sheet name but are nullable and not guaranteed unique across an
     * event, so the name falls back to the dog and is suffixed until the workbook accepts it.
     */
    private String competitorSheetName(Workbook workbook, FetchClassificationCompetitorDTO competitor) {
        String base = competitor.competitorNumber() != null
                ? String.valueOf(competitor.competitorNumber())
                : competitor.dogName() != null ? competitor.dogName() : competitor.dogIdentification();
        String name = WorkbookUtil.createSafeSheetName(base == null ? "-" : base);
        String candidate = name;
        for (int suffix = 2; workbook.getSheet(candidate) != null; suffix++) {
            candidate = WorkbookUtil.createSafeSheetName(name + " (" + suffix + ")");
        }
        return candidate;
    }

    private void writeConfiguration(Workbook workbook, CellStyle headerStyle, FetchEventDetailDTO event) {
        Sheet sheet = createSheet(workbook, "export.sheet.configuration");
        writeHeader(sheet, headerStyle, List.of(
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

        autoSize(sheet, 3);
    }

    private int writeConfigurationRow(Sheet sheet, int rowIndex, String labelKey, String id, String name) {
        Row row = sheet.createRow(rowIndex);
        setText(row, 0, translate(labelKey));
        setText(row, 1, id);
        setText(row, 2, name);
        return rowIndex + 1;
    }

    private void writeJudges(Workbook workbook, CellStyle headerStyle, List<FetchObdxEventJudgeDTO> judges) {
        Sheet sheet = createSheet(workbook, "export.sheet.judges");
        writeHeader(sheet, headerStyle, List.of(
                translate("export.column.judge_id"), translate("export.column.judge"),
                translate("export.column.collector_email")));

        int rowIndex = 1;
        for (FetchObdxEventJudgeDTO judge : judges) {
            Row row = sheet.createRow(rowIndex++);
            setText(row, 0, judge.judgeId());
            setText(row, 1, judge.judgeName());
            setText(row, 2, judge.collectorEmail());
        }
        autoSize(sheet, 3);
    }

    private void writeExercises(Workbook workbook, CellStyle headerStyle, List<FetchEventExerciseDTO> exercises) {
        Sheet sheet = createSheet(workbook, "export.sheet.exercises");
        writeHeader(sheet, headerStyle, List.of(
                translate("export.column.exercise_id"), translate("export.column.exercise"),
                translate("export.column.order"), translate("export.column.judge_ids"),
                translate("export.column.judges"), translate("export.column.tags")));

        int rowIndex = 1;
        for (FetchEventExerciseDTO exercise : exercises) {
            Row row = sheet.createRow(rowIndex++);
            setText(row, 0, exercise.id());
            setText(row, 1, exercise.name());
            setNumber(row, 2, exercise.position());
            List<FetchObdxEventJudgeDTO> judges = exercise.judges() == null ? List.of() : exercise.judges();
            setText(row, 3, join(judges.stream().map(FetchObdxEventJudgeDTO::judgeId).toList()));
            setText(row, 4, join(judges.stream().map(FetchObdxEventJudgeDTO::judgeName).toList()));
            setText(row, 5, join(exercise.tags()));
        }
        autoSize(sheet, 6);
    }

    private void writeCompetitors(Workbook workbook, CellStyle headerStyle,
                                  List<FetchObdxEventCompetitorDTO> competitors) {
        Sheet sheet = createSheet(workbook, "export.sheet.competitors");
        writeHeader(sheet, headerStyle, List.of(
                translate("export.column.competitor_number"), translate("export.column.start_number"),
                translate("export.column.identification"), translate("export.column.origin"),
                translate("export.column.license"),
                translate("export.column.dog_name"), translate("export.column.handler"),
                translate("export.column.reserve"), translate("export.column.sex"),
                translate("export.column.bih"), translate("export.column.primer"),
                translate("export.column.country"), translate("export.column.team")));

        int rowIndex = 1;
        for (FetchObdxEventCompetitorDTO competitor : competitors) {
            Row row = sheet.createRow(rowIndex++);
            setNumber(row, 0, competitor.competitorNumber());
            setNumber(row, 1, competitor.startNumber());
            setText(row, 2, competitor.dogIdentification());
            setText(row, 3, competitor.dogOrigin());
            setText(row, 4, competitor.dogLicense());
            setText(row, 5, competitor.dogName());
            setText(row, 6, competitor.handler());
            setBoolean(row, 7, competitor.reserve());
            setText(row, 8, competitor.sex());
            setBoolean(row, 9, competitor.bih());
            setText(row, 10, competitor.primer());
            setText(row, 11, referenceNames.countryName(competitor.country()));
            setText(row, 12, competitor.team());
        }
        autoSize(sheet, 13);
    }

    private Sheet createSheet(Workbook workbook, String nameKey) {
        return workbook.createSheet(WorkbookUtil.createSafeSheetName(translate(nameKey)));
    }

    private void writeHeader(Sheet sheet, CellStyle style, List<String> headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(bold);
        return style;
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void setText(Row row, int column, String value) {
        if (value != null) {
            row.createCell(column).setCellValue(value);
        }
    }

    private void setNumber(Row row, int column, Number value) {
        if (value != null) {
            row.createCell(column).setCellValue(value.doubleValue());
        }
    }

    /**
     * Booleans are written as the localised "yes"/"no" rather than as Excel TRUE/FALSE cells: the workbook is
     * read by people, and Excel renders a boolean cell in its own UI language, not the requested one.
     */
    private void setBoolean(Row row, int column, Boolean value) {
        if (value != null) {
            row.createCell(column).setCellValue(translate(value ? "export.value.yes" : "export.value.no"));
        }
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
}
