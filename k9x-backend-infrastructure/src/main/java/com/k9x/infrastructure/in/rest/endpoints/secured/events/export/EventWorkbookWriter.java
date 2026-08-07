package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Renders the private event detail as an xlsx workbook with four sheets: Configuration, Judges, Exercises and
 * Competitors. Sheet names and column headers are resolved through the {@link MessageSource} using the request
 * locale, the same way {@code GetEvent} resolves breed and discipline names.
 */
public class EventWorkbookWriter {

    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String MULTI_VALUE_SEPARATOR = ", ";

    private final MessageSource messageSource;

    public EventWorkbookWriter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Uses the in-memory {@link XSSFWorkbook} rather than the streaming SXSSF variant on purpose: SXSSF
     * stores every string inline yet still emits (and declares) an empty {@code xl/sharedStrings.xml}, and
     * both Excel and LibreOffice reject the resulting workbook — Excel offers to "repair" it and rewrites
     * cells while doing so. An event holds a few hundred rows at most, so streaming buys nothing here.
     */
    public byte[] write(FetchEventDetailDTO event) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            writeConfiguration(workbook, headerStyle, event);
            writeJudges(workbook, headerStyle, event.judges());
            writeExercises(workbook, headerStyle, event.exercises());
            writeCompetitors(workbook, headerStyle, event.competitors());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
                obdx == null ? null : obdx.discipline(), resolveDiscipline(obdx == null ? null : obdx.discipline()));
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
                translate("export.column.chip"), translate("export.column.identifier"),
                translate("export.column.dog_name"), translate("export.column.handler"),
                translate("export.column.reserve"), translate("export.column.sex"),
                translate("export.column.bih"), translate("export.column.country"),
                translate("export.column.team")));

        int rowIndex = 1;
        for (FetchObdxEventCompetitorDTO competitor : competitors) {
            Row row = sheet.createRow(rowIndex++);
            setNumber(row, 0, competitor.competitorNumber());
            setNumber(row, 1, competitor.startNumber());
            setText(row, 2, competitor.dogIdentity());
            setText(row, 3, competitor.dogId());
            setText(row, 4, competitor.dogName());
            setText(row, 5, competitor.handler());
            setBoolean(row, 6, competitor.reserve());
            setText(row, 7, competitor.sex());
            setBoolean(row, 8, competitor.bih());
            setText(row, 9, competitor.country());
            setText(row, 10, competitor.team());
        }
        autoSize(sheet, 11);
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

    private void setBoolean(Row row, int column, Boolean value) {
        if (value != null) {
            row.createCell(column).setCellValue(value);
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

    private String resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        return translate("discipline." + disciplineId.toUpperCase(Locale.ROOT) + ".name");
    }

    private String translate(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
