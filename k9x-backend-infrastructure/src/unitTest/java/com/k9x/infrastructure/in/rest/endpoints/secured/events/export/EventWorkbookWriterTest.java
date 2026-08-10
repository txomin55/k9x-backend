package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationExerciseScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationJudgeScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class EventWorkbookWriterTest {

    private static final long DEADLINE = 1735689600000L; // 2025-01-01T00:00:00Z
    private static final Map<String, BigDecimal> COEFFICIENTS = Map.of("ex-1", new BigDecimal("4"));

    private EventWorkbookWriter writer;

    @BeforeEach
    void setUp() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("export.sheet.configuration", Locale.ENGLISH, "Configuration");
        messageSource.addMessage("export.sheet.judges", Locale.ENGLISH, "Judges");
        messageSource.addMessage("export.sheet.exercises", Locale.ENGLISH, "Exercises");
        messageSource.addMessage("export.sheet.competitors", Locale.ENGLISH, "Competitors");
        messageSource.addMessage("export.column.competitor_number", Locale.ENGLISH, "Competitor number");
        messageSource.addMessage("export.field.federation", Locale.ENGLISH, "Federation");
        messageSource.addMessage("discipline.OBDX.name", Locale.ENGLISH, "Obedience");
        messageSource.addMessage("breed.breed-1.name", Locale.ENGLISH, "Border Collie");
        messageSource.addMessage("country.es.name", Locale.ENGLISH, "Spain");
        messageSource.addMessage("export.value.yes", Locale.ENGLISH, "yes");
        messageSource.addMessage("export.value.no", Locale.ENGLISH, "no");
        messageSource.setUseCodeAsDefaultMessage(true);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        writer = new EventWorkbookWriter(messageSource, new ReferenceNameResolver(messageSource));
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    private FetchEventDetailDTO event() {
        FetchObdxEventDTO obdx = new FetchObdxEventDTO("event-1", "Spring Cup", "stage-1", "Stage A", "OBDX",
                "STARTED", DEADLINE, ObdxAvgMethod.MID_AVG, List.of());
        FetchObdxEventJudgeDTO judge = new FetchObdxEventJudgeDTO("judge-1", "Ana", "collector@k9x.com");
        FetchEventExerciseDTO exercise = new FetchEventExerciseDTO("ex-1", "Heelwork", 1,
                List.of("tag-a", "tag-b"), List.of(judge));
        FetchObdxEventCompetitorDTO competitor = new FetchObdxEventCompetitorDTO("dog-1", "Rex", "origin-999", "LIC-999", "breed-1",
                "Owner", "Handler", "Team A", "ES", "MALE", (short) 3, (short) 7, true, "STARTED", true, "CART-999", false);
        FetchEventConfigurationDTO configuration = new FetchEventConfigurationDTO("OBDX.FCI_GRADE_1.V0", "Grade 1",
                new FederationInfoDTO("FCI", "FCI"));

        return new FetchEventDetailDTO(obdx, List.of(competitor), List.of(exercise), List.of(judge), configuration);
    }

    private XSSFWorkbook read(byte[] bytes) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    /** Null for a missing or non-text cell, so scanning mixed rows never throws. */
    private String text(Sheet sheet, int row, int column) {
        Row r = sheet.getRow(row);
        Cell cell = r == null ? null : r.getCell(column);
        return cell == null || cell.getCellType() != CellType.STRING ? null : cell.getStringCellValue();
    }

    @Test
    void writes_the_four_sheets_with_translated_names() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(workbook.getSheetName(0)).isEqualTo("Configuration");
            assertThat(workbook.getSheetName(1)).isEqualTo("Judges");
            assertThat(workbook.getSheetName(2)).isEqualTo("Exercises");
            assertThat(workbook.getSheetName(3)).isEqualTo("Competitors");
        }
    }

    @Test
    void writes_configuration_with_ids_next_to_names() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event()))) {
            Sheet sheet = workbook.getSheetAt(0);

            assertThat(text(sheet, 1, 1)).isEqualTo("event-1");
            assertThat(text(sheet, 1, 2)).isEqualTo("Spring Cup");
            assertThat(text(sheet, 2, 1)).isEqualTo("OBDX");
            assertThat(text(sheet, 2, 2)).isEqualTo("Obedience");
            assertThat(text(sheet, 3, 0)).isEqualTo("Federation");
            assertThat(text(sheet, 3, 1)).isEqualTo("FCI");
            assertThat(text(sheet, 4, 1)).isEqualTo("OBDX.FCI_GRADE_1.V0");
            assertThat(text(sheet, 4, 2)).isEqualTo("Grade 1");
            // the deadline is rendered as a UTC day, never as a timezone-sensitive date cell
            assertThat(text(sheet, 5, 2)).isEqualTo("2025-01-01");
        }
    }

    @Test
    void writes_judges_and_exercises_with_ids() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event()))) {
            Sheet judges = workbook.getSheetAt(1);
            assertThat(text(judges, 1, 0)).isEqualTo("judge-1");
            assertThat(text(judges, 1, 1)).isEqualTo("Ana");
            assertThat(text(judges, 1, 2)).isEqualTo("collector@k9x.com");

            Sheet exercises = workbook.getSheetAt(2);
            assertThat(text(exercises, 1, 0)).isEqualTo("ex-1");
            assertThat(text(exercises, 1, 1)).isEqualTo("Heelwork");
            assertThat(exercises.getRow(1).getCell(2).getNumericCellValue()).isEqualTo(1d);
            assertThat(text(exercises, 1, 3)).isEqualTo("judge-1");
            assertThat(text(exercises, 1, 4)).isEqualTo("Ana");
            assertThat(text(exercises, 1, 5)).isEqualTo("tag-a, tag-b");
        }
    }

    /**
     * Exercise ids share a long common prefix and differ only in the trailing digits, so a bug that reused
     * one row's string for the next would look plausible on screen. Writes the real ids of an FCI grade 3
     * event, in the running order an organizer actually reordered them into, and demands each one back
     * verbatim next to its own name.
     */
    @Test
    void writes_each_exercise_id_verbatim() throws IOException {
        List<String> ids = List.of(
                "OBDX.FCI_GRADE_3.8_V0", "OBDX.FCI_GRADE_3.3_V0", "OBDX.FCI_GRADE_3.4_V0",
                "OBDX.FCI_GRADE_3.7_V0", "OBDX.FCI_GRADE_3.5_V0", "OBDX.FCI_GRADE_3.10_V0",
                "OBDX.FCI_GRADE_3.9_V0", "OBDX.FCI_GRADE_3.6_V0", "OBDX.FCI_GRADE_3.1_V0",
                "OBDX.FCI_GRADE_3.2_V0");

        List<FetchEventExerciseDTO> exercises = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            exercises.add(new FetchEventExerciseDTO(ids.get(i), "Name " + i, i + 1, List.of(), List.of()));
        }

        FetchEventDetailDTO event = event();
        FetchEventDetailDTO withExercises = new FetchEventDetailDTO(event.obdx(), event.competitors(),
                exercises, event.judges(), event.configuration());

        try (XSSFWorkbook workbook = read(writer.write(withExercises))) {
            Sheet sheet = workbook.getSheetAt(2);
            for (int i = 0; i < ids.size(); i++) {
                assertThat(text(sheet, i + 1, 0)).isEqualTo(ids.get(i));
                assertThat(text(sheet, i + 1, 1)).isEqualTo("Name " + i);
            }
        }
    }

    /**
     * Guards the workbook against the shape Excel and LibreOffice refuse to open. The streaming SXSSF
     * writer keeps every string inline and still ships an empty shared-string table; both readers then
     * treat the file as damaged, and Excel silently rewrites cells while "repairing" it. POI itself reads
     * that file back happily, so only the packaging can tell the two apart.
     */
    @Test
    void packages_strings_in_the_shared_string_table() throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(writer.write(event())))) {
            String sharedStrings = null;
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals("xl/sharedStrings.xml")) {
                    sharedStrings = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            assertThat(sharedStrings).isNotNull();
            assertThat(sharedStrings).doesNotContain("count=\"0\"");
            assertThat(sharedStrings).contains("Spring Cup");
        }
    }

    private FetchClassificationDTO classification() {
        FetchClassificationJudgeScoreDTO judgeScore =
                new FetchClassificationJudgeScoreDTO("judge-1", "Ana", new BigDecimal("9.5"), null, true);
        FetchClassificationExerciseScoreDTO exercise = new FetchClassificationExerciseScoreDTO(
                "ex-1", (short) 1, List.of(), new BigDecimal("19.0"), new BigDecimal("19.0"),
                new BigDecimal("95.00"), List.of(judgeScore), List.of(), null);
        FetchClassificationCompetitorDTO competitor = new FetchClassificationCompetitorDTO(
                "dog-1", "Rex", "breed-1", "Owner", "Handler", "Team A", "ES", (short) 3, (short) 7, 1,
                new BigDecimal("285.5"), new BigDecimal("95.17"), false, "SETTLED", true, false, false,
                List.of(exercise), List.of(), "EXC", null);
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(0L, List.of(competitor), "MID_AVG", List.of());

        return new FetchClassificationDTO("event-1", "Spring Cup", "FINISHED", "stage-1", "Stage A", "Cup",
                "OBDX", "OBDX.FCI_GRADE_1.V0", "Grade 1", 0L, obdx, "A");
    }

    @Test
    void omits_the_classification_sheets_when_no_classification_is_requested() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event(), null, Map.of()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
        }
    }

    @Test
    void writes_the_classification_sheet_with_identification_score_percentage_and_acronym() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event(), classification(), COEFFICIENTS))) {
            Sheet sheet = workbook.getSheet("export.sheet.classification");
            assertThat(sheet).isNotNull();

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1d);   // position
            assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(7d);   // competitor number
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("dog-1");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Rex");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Handler");
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Team A");
            assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(285.5d);
            assertThat(row.getCell(7).getNumericCellValue()).isEqualTo(95.17d);
            assertThat(row.getCell(8).getStringCellValue()).isEqualTo("EXC");
        }
    }

    @Test
    void writes_one_sheet_per_competitor_named_after_the_competitor_number() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event(), classification(), COEFFICIENTS))) {
            Sheet sheet = workbook.getSheet("7");
            assertThat(sheet).isNotNull();

            String flattened = flatten(sheet);
            assertThat(flattened).contains("Rex").contains("dog-1").contains("origin-999").contains("Handler").contains("Team A");
            // per-judge and per-exercise scores, by exercise name rather than id, then the totals
            assertThat(flattened).contains("Heelwork").contains("Ana").contains("9.5");
            assertThat(flattened).contains("285.5").contains("95.17").contains("EXC");
            // the individual sheet carries no ids and no per-exercise percentage
            assertThat(flattened).doesNotContain("ex-1").doesNotContain("judge-1");
        }
    }

    /**
     * The classification carries the breed and the country as bare ids, the same way the read endpoints get
     * them; the workbook is read by people, so it must show the localised names instead.
     */
    @Test
    void writes_the_competitor_breed_and_country_by_name_and_not_by_id() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event(), classification(), COEFFICIENTS))) {
            String flattened = flatten(workbook.getSheet("7"));

            assertThat(flattened).contains("Border Collie").doesNotContain("breed-1");
            assertThat(flattened).contains("Spain");
        }
    }

    private String flatten(Sheet sheet) {
        StringBuilder text = new StringBuilder();
        for (Row row : sheet) {
            for (Cell cell : row) {
                text.append(switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> "";
                }).append('|');
            }
        }
        return text.toString();
    }

    /**
     * The scores table is pivoted: one row per exercise with a column per judge. Two judges scoring the same
     * exercise must land on the same row, side by side, not on two rows repeating the exercise name.
     */
    @Test
    void pivots_the_scores_table_with_one_column_per_judge() throws IOException {
        FetchObdxEventJudgeDTO second = new FetchObdxEventJudgeDTO("judge-2", "Bea", null);
        FetchEventDetailDTO event = event();
        FetchEventDetailDTO twoJudges = new FetchEventDetailDTO(event.obdx(), event.competitors(),
                event.exercises(), List.of(event.judges().getFirst(), second), event.configuration());

        FetchClassificationExerciseScoreDTO exercise = new FetchClassificationExerciseScoreDTO(
                // exerciseScore is the MAXIMUM attainable (10 x coef 4); totalScore is what was achieved
                "ex-1", (short) 1, List.of("group", "static"), new BigDecimal("40.0"), new BigDecimal("29.0"),
                new BigDecimal("72.50"),
                List.of(new FetchClassificationJudgeScoreDTO("judge-1", "Ana", new BigDecimal("7.5"), null, true),
                        new FetchClassificationJudgeScoreDTO("judge-2", "Bea", new BigDecimal("7"), null, true)),
                List.of(), null);
        FetchClassificationCompetitorDTO competitor = new FetchClassificationCompetitorDTO(
                "dog-1", "Rex", "breed-1", "Owner", "Handler", "Team A", "ES", (short) 3, (short) 7, 1,
                new BigDecimal("18.0"), new BigDecimal("90.00"), false, "SETTLED", true, false, false,
                List.of(exercise), List.of(), "EXC", null);
        FetchClassificationDTO classification = new FetchClassificationDTO("event-1", "Spring Cup", "FINISHED",
                "stage-1", "Stage A", "Cup", "OBDX", "OBDX.FCI_GRADE_1.V0", "Grade 1", 0L,
                new FetchObdxClassificationDTO(0L, List.of(competitor), "MID_AVG", List.of()), "A");

        try (XSSFWorkbook workbook = read(writer.write(twoJudges, classification, COEFFICIENTS))) {
            Sheet sheet = workbook.getSheet("7");

            int headerRow = -1;
            for (Row row : sheet) {
                if ("export.column.order".equals(text(sheet, row.getRowNum(), 0))) {
                    headerRow = row.getRowNum();
                }
            }
            assertThat(headerRow).isPositive();

            // Order | Exercise | Tags | Ana | Bea | Average | Coefficient | Points
            assertThat(text(sheet, headerRow, 3)).isEqualTo("Ana");
            assertThat(text(sheet, headerRow, 4)).isEqualTo("Bea");

            Row scores = sheet.getRow(headerRow + 1);
            assertThat(scores.getCell(0).getNumericCellValue()).isEqualTo(1d);
            assertThat(scores.getCell(1).getStringCellValue()).isEqualTo("Heelwork");
            assertThat(scores.getCell(2).getStringCellValue()).isEqualTo("group, static");
            assertThat(scores.getCell(3).getNumericCellValue()).isEqualTo(7.5d);
            assertThat(scores.getCell(4).getNumericCellValue()).isEqualTo(7d);
            // the average is the judges' own average, never the maximum attainable divided by the coefficient
            assertThat(scores.getCell(5).getNumericCellValue()).isEqualTo(7.25d);
            assertThat(scores.getCell(6).getNumericCellValue()).isEqualTo(4d);
            assertThat(scores.getCell(7).getNumericCellValue()).isEqualTo(29.0d);
        }
    }

    @Test
    void writes_competitor_columns_in_order() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event()))) {
            Sheet sheet = workbook.getSheetAt(3);

            assertThat(text(sheet, 0, 0)).isEqualTo("Competitor number");

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(7d);   // competitor number
            assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(3d);   // position = start number
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("dog-1");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("origin-999");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("LIC-999");
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Rex");
            assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Handler");
            assertThat(row.getCell(7).getStringCellValue()).isEqualTo("no");   // reserve, as localised text
            assertThat(row.getCell(8).getStringCellValue()).isEqualTo("MALE");
            assertThat(row.getCell(9).getStringCellValue()).isEqualTo("yes");  // bih
            assertThat(row.getCell(10).getStringCellValue()).isEqualTo("CART-999");  // primer
            assertThat(row.getCell(11).getStringCellValue()).isEqualTo("Spain");  // country, by name and not by code
            assertThat(row.getCell(12).getStringCellValue()).isEqualTo("Team A");
        }
    }

    @Test
    void writes_the_workbook_when_the_configuration_could_not_be_resolved() throws IOException {
        FetchEventDetailDTO event = event();
        FetchEventDetailDTO withoutConfiguration = new FetchEventDetailDTO(event.obdx(), event.competitors(),
                event.exercises(), event.judges(), null);

        try (XSSFWorkbook workbook = read(writer.write(withoutConfiguration))) {
            Sheet sheet = workbook.getSheetAt(0);

            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(text(sheet, 3, 1)).isNull();
            assertThat(text(sheet, 4, 1)).isNull();
            assertThat(text(sheet, 5, 2)).isEqualTo("2025-01-01");
        }
    }
}
