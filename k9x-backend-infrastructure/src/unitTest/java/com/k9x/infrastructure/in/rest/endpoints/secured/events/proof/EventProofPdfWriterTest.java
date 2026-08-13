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
    /** Strips are packed onto landscape A4 and cut apart. */
    private static final float A4_WIDTH = 29.7f * CM;
    private static final float A4_HEIGHT = 21f * CM;
    /** 0.5 cm sheet margin, 6 cm blocks and a 0.5 cm cut gap leave room for three strips per sheet. */
    private static final int STRIPS_PER_SHEET = 3;

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

    private List<EventProofData> competitors(int count) {
        List<EventProofData> proofs = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            proofs.add(new EventProofData("Spring Cup", STAGE_DATE_FROM, "ADECAN", "Rota (Cádiz)", "3", "233,25",
                    "VERY GOOD", "Nola Kaschubowski", i, "Handler " + i, "Dog " + i,
                    List.of(new EventProofData.Judge("Ingrid Tamášiová", true))));
        }
        return proofs;
    }

    private static List<EventProofData.Judge> judges(int count) {
        List<EventProofData.Judge> judges = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            judges.add(new EventProofData.Judge("Judge " + i, i == 1));
        }
        return judges;
    }

    @Test
    void writes_landscape_a4_sheets() throws IOException {
        PdfReader reader = new PdfReader(writer.write(List.of(proof())));
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPageSize(1).getWidth()).isCloseTo(A4_WIDTH, within());
            assertThat(reader.getPageSize(1).getHeight()).isCloseTo(A4_HEIGHT, within());
        } finally {
            reader.close();
        }
    }

    /** The whole point of packing: a 20-competitor event must not print 20 sheets. */
    @Test
    void packs_several_strips_on_each_sheet() throws IOException {
        byte[] pdf = writer.write(competitors(20));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(7); // 20 strips, 3 to a sheet
        } finally {
            reader.close();
        }
        String firstSheet = text(pdf, 1);
        for (int i = 1; i <= STRIPS_PER_SHEET; i++) {
            assertThat(firstSheet).contains("[#" + i + "]");
        }
        assertThat(firstSheet).doesNotContain("[#" + (STRIPS_PER_SHEET + 1) + "]");
    }

    /** Every competitor gets a strip, however many sheets that takes. */
    @Test
    void prints_a_strip_for_every_competitor() throws IOException {
        byte[] pdf = writer.write(competitors(9));

        String all = text(pdf, 1) + text(pdf, 2) + text(pdf, 3);
        for (int i = 1; i <= 9; i++) {
            assertThat(all).contains("[#" + i + "] Handler " + i + " [Dog " + i + "]");
        }
    }

    /** One competitor prints one strip: no duplicated values inside its own block. */
    @Test
    void prints_a_single_block_per_competitor() throws IOException {
        String text = text(writer.write(List.of(proof())));

        assertThat(occurrences(text, "15-02-2026")).isEqualTo(1);
        assertThat(occurrences(text, "[#1] Handler [Rex]")).isEqualTo(1);
        assertThat(occurrences(text, "233,25")).isEqualTo(1);
    }

    /**
     * A tall strip (many judges) takes the room it needs, pushing the following ones to the next sheet instead
     * of being squeezed or clipped.
     */
    @Test
    void moves_a_strip_that_does_not_fit_to_the_next_sheet() throws IOException {
        List<EventProofData> proofs = List.of(proof(judges(9)), proof(judges(9)), proof(judges(9)));

        byte[] pdf = writer.write(proofs);

        PdfReader reader = new PdfReader(pdf);
        try {
            // Only two 6.96 cm strips fit in the 20 cm of usable height, so the third starts a new sheet.
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
            assertThat(reader.getPageSize(2).getHeight()).isCloseTo(A4_HEIGHT, within());
        } finally {
            reader.close();
        }
    }

    /**
     * The core regression: a strip with many judges must keep every judge, in one piece. A flow-layout
     * implementation would break the table across sheets and drop signature boxes.
     */
    @Test
    void keeps_a_tall_strip_whole() throws IOException {
        byte[] pdf = writer.write(List.of(proof(judges(9))));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        } finally {
            reader.close();
        }
        String text = text(pdf);
        for (int i = 1; i <= 9; i++) {
            assertThat(text).contains("Judge " + i);
        }
    }

    /** With four judges or fewer a strip still fits the 6 cm the booklet block gives it, three to a sheet. */
    @Test
    void fits_three_strips_of_up_to_four_judges_on_one_sheet() throws IOException {
        PdfReader reader = new PdfReader(writer.write(List.of(proof(judges(4)), proof(judges(4)),
                proof(judges(4)))));
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        } finally {
            reader.close();
        }
    }

    /** Judge names are not Latin-1. The built-in WinAnsi Helvetica would silently drop these glyphs. */
    @Test
    void keeps_non_latin1_names_intact() throws IOException {
        String text = text(writer.write(List.of(proof())));

        assertThat(text).contains("Tamášiová");
        assertThat(text).contains("Peña");
    }

    /** Guards against someone "simplifying" the font back to a non-embedded, single-byte encoded one. */
    @Test
    void embeds_the_font_as_an_identity_h_subset() throws IOException {
        PdfReader reader = new PdfReader(writer.write(List.of(proof())));
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
        String text = text(writer.write(List.of(proof(List.of(new EventProofData.Judge("Ana", false),
                new EventProofData.Judge("Bea", true))))));

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
        String text = text(writer.write(List.of(proof(List.of(new EventProofData.Judge("Ana", false),
                new EventProofData.Judge("Bea", false))))));

        assertThat(occurrences(text, "MAIN JUDGE")).isEqualTo(1);
        assertThat(text.indexOf("MAIN JUDGE")).isLessThan(text.indexOf("Ana"));
        assertThat(text.indexOf("Ana")).isLessThan(text.indexOf("Bea"));
    }

    @Test
    void prints_the_commissioner_in_the_steward_cell() throws IOException {
        String text = text(writer.write(List.of(proof())));

        assertThat(occurrences(text, "CHIEF STEWARD")).isEqualTo(1);
        assertThat(occurrences(text, "Nola Kaschubowski")).isEqualTo(1);
    }

    /** Events with no commissioner recorded still print the box, empty, to be filled in by hand. */
    @Test
    void leaves_the_steward_cell_empty_when_there_is_no_commissioner() throws IOException {
        EventProofData withoutCommissioner = new EventProofData("Spring Cup", STAGE_DATE_FROM, "ADECAN",
                "Rota (Cádiz)", "3", "233,25", "VERY GOOD", null, 1, "Handler", "Rex", List.of());

        String text = text(writer.write(List.of(withoutCommissioner)));

        assertThat(occurrences(text, "CHIEF STEWARD")).isEqualTo(1);
        assertThat(text).doesNotContain("Nola Kaschubowski");
    }

    @Test
    void writes_a_syntactically_valid_pdf() {
        byte[] pdf = writer.write(List.of(proof()));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).endsWith("%%EOF\n");
    }

    /**
     * A brand new event has no scores, no judges and possibly no address; the organizer still prints the strips
     * to fill in by hand, so every box must degrade to empty instead of throwing.
     */
    @Test
    void writes_the_document_when_every_optional_value_is_missing() throws IOException {
        byte[] pdf = writer.write(List.of(new EventProofData(null, null, null, null, null, null, null, null,
                null, null, null, null)));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPageSize(1).getHeight()).isCloseTo(A4_HEIGHT, within());
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
        return text(pdf, 1);
    }

    private String text(byte[] pdf, int page) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(reader).getTextFromPage(page);
        } finally {
            reader.close();
        }
    }
}
