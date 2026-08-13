package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class EventProofPdfWriterTest {

    /** 2026-02-15T10:00:00Z — a UTC morning, so a timezone slip would show up as the 14th or the 16th. */
    private static final long STAGE_DATE_FROM = 1771149600000L;
    private static final float CM = 72f / 2.54f;
    private static final float EXPECTED_WIDTH = 20.5f * CM;
    /** The page is one 20.5 x 6 cm block plus the 0.4 cm margin around it, printed at 100 %. */
    private static final float MIN_EXPECTED_HEIGHT = 6f * CM + 2 * 0.4f * CM;

    private EventProofPdfWriter writer;

    @BeforeEach
    void setUp() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("proof.label.date", Locale.ENGLISH, "DATE");
        messageSource.addMessage("proof.label.organizer", Locale.ENGLISH, "ORGANIZING GROUP");
        messageSource.addMessage("proof.label.locality", Locale.ENGLISH, "LOCALITY");
        messageSource.addMessage("proof.label.class", Locale.ENGLISH, "CLASS");
        messageSource.addMessage("proof.label.score", Locale.ENGLISH, "SCORE");
        messageSource.addMessage("proof.label.qualification", Locale.ENGLISH, "QUALIFICATION");
        messageSource.addMessage("proof.label.steward", Locale.ENGLISH, "CHIEF STEWARD");
        messageSource.addMessage("proof.label.main_judge", Locale.ENGLISH, "MAIN JUDGE");
        messageSource.addMessage("proof.label.judge", Locale.ENGLISH, "JUDGE");
        messageSource.addMessage("proof.label.observations", Locale.ENGLISH, "REMARKS");
        messageSource.setUseCodeAsDefaultMessage(true);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        writer = new EventProofPdfWriter(messageSource);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    private EventProofData proof(List<EventProofData.Judge> judges) {
        return new EventProofData("Spring Cup", STAGE_DATE_FROM, "ADECAN", "Rota (Cádiz)", "3", "233,25",
                "VERY GOOD", "Nola Kaschubowski", 1, "Handler", "Rex", judges);
    }

    private EventProofData proof() {
        return proof(List.of(new EventProofData.Judge("Ingrid Tamášiová", true),
                new EventProofData.Judge("Javier Peña", false)));
    }

    private static List<EventProofData.Judge> judges(int count) {
        List<EventProofData.Judge> judges = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            judges.add(new EventProofData.Judge("Judge " + i, i == 1));
        }
        return judges;
    }

    @Test
    void writes_a_single_page_of_the_expected_size() throws IOException {
        PdfReader reader = new PdfReader(writer.write(proof()));
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPageSize(1).getWidth()).isCloseTo(EXPECTED_WIDTH, within());
            assertThat(reader.getPageSize(1).getHeight()).isCloseTo(MIN_EXPECTED_HEIGHT, within());
        } finally {
            reader.close();
        }
    }

    /** One strip per download: the organizer glues one block per event, spares are not wanted. */
    @Test
    void prints_a_single_block() throws IOException {
        String text = text(writer.write(proof()));

        assertThat(occurrences(text, "15-02-2026")).isEqualTo(1);
        assertThat(occurrences(text, "[#1] Handler [Rex]")).isEqualTo(1);
        assertThat(occurrences(text, "233,25")).isEqualTo(1);
    }

    /**
     * The core regression: with many judges the strip grows, and a flow-layout implementation would spill the
     * overflow onto a second page or drop it. The page must grow instead, keeping every judge on one sheet.
     */
    @Test
    void grows_the_page_instead_of_spilling_to_a_second_one() throws IOException {
        byte[] pdf = writer.write(proof(judges(9)));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPageSize(1).getWidth()).isCloseTo(EXPECTED_WIDTH, within());
            assertThat(reader.getPageSize(1).getHeight()).isGreaterThan(MIN_EXPECTED_HEIGHT);
        } finally {
            reader.close();
        }
        String text = text(pdf);
        for (int i = 1; i <= 9; i++) {
            assertThat(text).contains("Judge " + i);
        }
    }

    /** With four judges or fewer the strip still fits the 6 cm the paper booklet gives it. */
    @Test
    void keeps_the_page_at_its_paper_size_with_up_to_four_judges() throws IOException {
        PdfReader reader = new PdfReader(writer.write(proof(judges(4))));
        try {
            assertThat(reader.getPageSize(1).getHeight()).isCloseTo(MIN_EXPECTED_HEIGHT, within());
        } finally {
            reader.close();
        }
    }

    /** Judge names are not Latin-1. The built-in WinAnsi Helvetica would silently drop these glyphs. */
    @Test
    void keeps_non_latin1_names_intact() throws IOException {
        String text = text(writer.write(proof()));

        assertThat(text).contains("Tamášiová");
        assertThat(text).contains("Peña");
    }

    /** Guards against someone "simplifying" the font back to a non-embedded, single-byte encoded one. */
    @Test
    void embeds_the_font_as_an_identity_h_subset() throws IOException {
        PdfReader reader = new PdfReader(writer.write(proof()));
        try {
            PdfDictionary fonts = reader.getPageN(1)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.FONT);
            assertThat(fonts).isNotNull();
            boolean embeddedIdentityH = fonts.getKeys().stream()
                    .map(fonts::getAsDict)
                    .anyMatch(EventProofPdfWriterTest::isEmbeddedIdentityH);
            assertThat(embeddedIdentityH).isTrue();
        } finally {
            reader.close();
        }
    }

    /** The judge flagged in the database takes the "juez principal" box, whatever order it arrives in. */
    @Test
    void puts_the_flagged_judge_in_the_main_judge_box() throws IOException {
        String text = text(writer.write(proof(List.of(new EventProofData.Judge("Ana", false),
                new EventProofData.Judge("Bea", true)))));

        assertThat(occurrences(text, "MAIN JUDGE")).isEqualTo(1);
        assertThat(text.indexOf("MAIN JUDGE")).isLessThan(text.indexOf("Bea"));
        assertThat(text.indexOf("Bea")).isLessThan(text.indexOf("Ana"));
    }

    /**
     * The paper box exists whether or not anybody was flagged, so with no main judge the first one fills it —
     * printing an unlabelled strip would leave the organizer guessing.
     */
    @Test
    void falls_back_to_the_first_judge_when_none_is_flagged() throws IOException {
        String text = text(writer.write(proof(List.of(new EventProofData.Judge("Ana", false),
                new EventProofData.Judge("Bea", false)))));

        assertThat(occurrences(text, "MAIN JUDGE")).isEqualTo(1);
        assertThat(text.indexOf("MAIN JUDGE")).isLessThan(text.indexOf("Ana"));
        assertThat(text.indexOf("Ana")).isLessThan(text.indexOf("Bea"));
    }

    @Test
    void prints_the_commissioner_in_the_steward_cell() throws IOException {
        String text = text(writer.write(proof()));

        assertThat(occurrences(text, "CHIEF STEWARD")).isEqualTo(1);
        assertThat(occurrences(text, "Nola Kaschubowski")).isEqualTo(1);
    }

    /** Events with no commissioner recorded still print the box, empty, to be filled in by hand. */
    @Test
    void leaves_the_steward_cell_empty_when_there_is_no_commissioner() throws IOException {
        EventProofData withoutCommissioner = new EventProofData("Spring Cup", STAGE_DATE_FROM, "ADECAN",
                "Rota (Cádiz)", "3", "233,25", "VERY GOOD", null, 1, "Handler", "Rex", List.of());

        String text = text(writer.write(withoutCommissioner));

        assertThat(occurrences(text, "CHIEF STEWARD")).isEqualTo(1);
        assertThat(text).doesNotContain("Nola Kaschubowski");
    }

    @Test
    void writes_a_syntactically_valid_pdf() {
        byte[] pdf = writer.write(proof());

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).endsWith("%%EOF\n");
    }

    /**
     * A brand new event has no scores, no judges and possibly no address; the organizer still prints the strips
     * to fill in by hand, so every box must degrade to empty instead of throwing.
     */
    @Test
    void writes_the_document_when_every_optional_value_is_missing() throws IOException {
        byte[] pdf = writer.write(new EventProofData(null, null, null, null, null, null, null, null, null, null,
                null, null));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPageSize(1).getHeight()).isCloseTo(MIN_EXPECTED_HEIGHT, within());
        } finally {
            reader.close();
        }
        assertThat(text(pdf)).contains("REMARKS");
    }

    private static boolean isEmbeddedIdentityH(PdfDictionary font) {
        if (font == null || !PdfName.TYPE0.equals(font.getAsName(PdfName.SUBTYPE))) {
            return false;
        }
        PdfObject encoding = font.get(PdfName.ENCODING);
        if (encoding == null || !"/Identity-H".equals(encoding.toString())) {
            return false;
        }
        PdfDictionary descendant = font.getAsArray(PdfName.DESCENDANTFONTS).getAsDict(0);
        return descendant != null && descendant.getAsDict(PdfName.FONTDESCRIPTOR) != null
                && descendant.getAsDict(PdfName.FONTDESCRIPTOR).get(PdfName.FONTFILE2) != null;
    }

    private static org.assertj.core.data.Offset<Float> within() {
        return org.assertj.core.data.Offset.offset(0.05f);
    }

    private static int occurrences(String text, String value) {
        int count = 0;
        int index = text.indexOf(value);
        while (index >= 0) {
            count++;
            index = text.indexOf(value, index + value.length());
        }
        return count;
    }

    private String text(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        } finally {
            reader.close();
        }
    }
}
