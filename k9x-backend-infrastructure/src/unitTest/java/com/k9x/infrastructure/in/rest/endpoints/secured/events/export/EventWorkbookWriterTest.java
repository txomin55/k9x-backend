package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class EventWorkbookWriterTest {

    private static final long DEADLINE = 1735689600000L; // 2025-01-01T00:00:00Z

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
        messageSource.setUseCodeAsDefaultMessage(true);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        writer = new EventWorkbookWriter(messageSource);
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
        FetchObdxEventCompetitorDTO competitor = new FetchObdxEventCompetitorDTO("dog-1", "Rex", "chip-999", "breed-1",
                "Owner", "Handler", "Team A", "ES", "MALE", (short) 3, (short) 7, true, "STARTED", true, false);
        FetchEventConfigurationDTO configuration = new FetchEventConfigurationDTO("OBDX.FCI_GRADE_1.V0", "Grade 1",
                new FederationInfoDTO("FCI", "FCI"));

        return new FetchEventDetailDTO(obdx, List.of(competitor), List.of(exercise), List.of(judge), configuration);
    }

    private XSSFWorkbook read(byte[] bytes) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private String text(Sheet sheet, int row, int column) {
        Row r = sheet.getRow(row);
        return r == null || r.getCell(column) == null ? null : r.getCell(column).getStringCellValue();
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

    @Test
    void writes_competitor_columns_in_order() throws IOException {
        try (XSSFWorkbook workbook = read(writer.write(event()))) {
            Sheet sheet = workbook.getSheetAt(3);

            assertThat(text(sheet, 0, 0)).isEqualTo("Competitor number");

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(7d);   // competitor number
            assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(3d);   // position = start number
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("chip-999");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("dog-1");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Rex");
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Handler");
            assertThat(row.getCell(6).getBooleanCellValue()).isFalse();       // reserve
            assertThat(row.getCell(7).getStringCellValue()).isEqualTo("MALE");
            assertThat(row.getCell(8).getBooleanCellValue()).isTrue();        // bih
            assertThat(row.getCell(9).getStringCellValue()).isEqualTo("ES");
            assertThat(row.getCell(10).getStringCellValue()).isEqualTo("Team A");
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
