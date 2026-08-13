package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the printable proof a competitor glues into its paper working booklet (cartilla de trabajo).
 *
 * <p>The booklet page holds three pre-printed 20.5 x 6 cm blocks, one per event, filled in by hand. This
 * writer produces exactly one strip reproducing those boxes with the event's data, and the page is that strip
 * plus a thin margin all around: the 20.5 x 6 cm block is printed at 100 %, cut out and glued over the block.
 *
 * <p>Two decisions are load-bearing:
 * <ul>
 *     <li>the strip is drawn at absolute coordinates with
 *     {@link PdfPTable#writeSelectedRows(int, int, float, float, PdfContentByte)} rather than added to the
 *     document flow. Flow layout would break the table across pages as soon as it grew past the page, and a
 *     booklet proof split over two sheets is useless;</li>
 *     <li>the page grows instead of the content shrinking. An event can have any number of judges; judges are
 *     printed four per row, and every extra row makes the strip taller than 6 cm. Losing a judge's signature
 *     box would invalidate the proof, so overflowing is preferred to clipping.</li>
 * </ul>
 */
public class EventProofPdfWriter {

    /**
     * 1 cm in PDF points. The PDF user unit is 1/72 inch, so the geometry is authored in cm — the units the
     * paper booklet is measured in — and converted once, here.
     */
    private static final float CM = 72f / 2.54f;

    private static final float PAGE_WIDTH = 20.5f * CM;
    private static final float BLOCK_HEIGHT = 6f * CM;
    /** Breathing room on all four sides so the borders do not run into the paper edge, and room to cut along. */
    private static final float MARGIN = 0.4f * CM;
    private static final float BLOCK_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    /**
     * 24 equal grid columns is the smallest base that expresses both the three-cell row
     * (FECHA | GRUPO ORGANIZADOR | LOCALIDAD) and the four-cell rows with the uneven widths the paper booklet
     * uses, purely through colspans. No nested tables, so every border lines up.
     */
    private static final int GRID_COLUMNS = 24;
    private static final int DATE_SPAN = 6;
    private static final int ORGANIZER_SPAN = 10;
    private static final int LOCALITY_SPAN = 8;
    private static final int CLASS_SPAN = 4;
    private static final int SCORE_SPAN = 5;
    private static final int QUALIFICATION_SPAN = 5;
    private static final int STEWARD_SPAN = 10;
    private static final int JUDGE_SPAN = 6;
    private static final int JUDGES_PER_ROW = GRID_COLUMNS / JUDGE_SPAN;

    private static final float LABEL_ROW_HEIGHT = 0.42f * CM;
    private static final float VALUE_ROW_HEIGHT = 0.60f * CM;
    /**
     * The class / score / qualification row is also the chief steward's: that cell is printed empty and the
     * steward writes their name and signs in it, so the whole row needs pen room, not just a line of text.
     */
    private static final float STEWARD_ROW_HEIGHT = 1.10f * CM;
    private static final float OBSERVATIONS_ROW_HEIGHT = 0.70f * CM;
    /** Blank space under the printed name for a pen signature; the floor when judges wrap to many rows. */
    private static final float MIN_JUDGE_ROW_HEIGHT = 1.10f * CM;
    /** Everything but the judge rows: three label rows, both value rows and the observations line. */
    private static final float FIXED_ROWS_HEIGHT = 3 * LABEL_ROW_HEIGHT + VALUE_ROW_HEIGHT
            + STEWARD_ROW_HEIGHT + OBSERVATIONS_ROW_HEIGHT;

    private static final float BORDER_WIDTH = 0.6f;
    private static final float CELL_PADDING = 2f;
    private static final float LABEL_FONT_SIZE = 6.5f;
    private static final float VALUE_FONT_SIZE = 9f;
    private static final float MIN_VALUE_FONT_SIZE = 6.5f;
    private static final float FONT_STEP = 0.5f;
    private static final float OBSERVATIONS_FONT_SIZE = 8f;

    private static final DateTimeFormatter PROOF_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String OBSERVATIONS_FORMAT = "[#%s] %s [%s]";

    /**
     * Liberation Sans is metrically compatible with Helvetica — so the geometry above holds — and covers Latin
     * Extended-A, which the built-in WinAnsi Helvetica does not: a judge named "Tamášiová" would lose glyphs.
     * The name passed to OpenPDF must keep the {@code .ttf} suffix, that is what selects the TrueType parser.
     */
    private static final String REGULAR_FONT_RESOURCE = "/fonts/LiberationSans-Regular.ttf";
    private static final String BOLD_FONT_RESOURCE = "/fonts/LiberationSans-Bold.ttf";

    /**
     * Parsed once and shared: a {@link BaseFont} holds only the immutable font program, while the per-document
     * glyph-usage state that drives subsetting lives in OpenPDF's {@code FontDetails}, which every
     * {@link PdfWriter} creates for itself. Caching is left off on purpose so OpenPDF's global, name-keyed
     * font cache cannot hand this instance to unrelated code.
     */
    private static final BaseFont REGULAR = loadFont(REGULAR_FONT_RESOURCE);
    private static final BaseFont BOLD = loadFont(BOLD_FONT_RESOURCE);

    private final MessageSource messageSource;

    public EventProofPdfWriter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public byte[] write(EventProofData proof) {
        int judgeRows = Math.max(1, ceilDiv(proof.judges().size(), JUDGES_PER_ROW));
        // The slack of a strip with few judges is absorbed by its signature rows, so a one-row strip still
        // fills its 6 cm and only genuinely crowded strips push past it.
        float judgeRowHeight = Math.max(MIN_JUDGE_ROW_HEIGHT, (BLOCK_HEIGHT - FIXED_ROWS_HEIGHT) / judgeRows);

        PdfPTable block = buildBlock(proof, judgeRowHeight);
        block.setTotalWidth(BLOCK_WIDTH);
        block.setLockedWidth(true);
        float pageHeight = Math.max(BLOCK_HEIGHT, block.getTotalHeight()) + 2 * MARGIN;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(PAGE_WIDTH, pageHeight), 0, 0, 0, 0);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addTitle(nullToEmpty(proof.eventName()));
            document.open();
            block.writeSelectedRows(0, -1, MARGIN, pageHeight - MARGIN, writer.getDirectContent());
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not render the event proof", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    private PdfPTable buildBlock(EventProofData proof, float judgeRowHeight) {
        PdfPTable table = new PdfPTable(GRID_COLUMNS);
        table.setWidthPercentage(100);

        addLabelRow(table, List.of(
                new Span(translate("proof.label.date"), DATE_SPAN),
                new Span(translate("proof.label.organizer"), ORGANIZER_SPAN),
                new Span(translate("proof.label.locality"), LOCALITY_SPAN)));
        addValueRow(table, List.of(
                new Span(formatDate(proof.dateFrom()), DATE_SPAN),
                new Span(proof.organizerName(), ORGANIZER_SPAN),
                new Span(proof.address(), LOCALITY_SPAN)), VALUE_ROW_HEIGHT, Element.ALIGN_MIDDLE);
        addLabelRow(table, List.of(
                new Span(translate("proof.label.class"), CLASS_SPAN),
                new Span(translate("proof.label.score"), SCORE_SPAN),
                new Span(translate("proof.label.qualification"), QUALIFICATION_SPAN),
                new Span(translate("proof.label.steward"), STEWARD_SPAN)));
        addValueRow(table, List.of(
                new Span(proof.className(), CLASS_SPAN),
                new Span(proof.totalScore(), SCORE_SPAN),
                new Span(proof.qualification(), QUALIFICATION_SPAN),
                // Top-aligned: the steward's name is printed at the top of the box and signs underneath it.
                new Span(proof.commissioner(), STEWARD_SPAN)), STEWARD_ROW_HEIGHT, Element.ALIGN_TOP);
        addJudgeRows(table, proof.judges(), judgeRowHeight);
        addObservationsRows(table, proof);
        return table;
    }

    private void addLabelRow(PdfPTable table, List<Span> labels) {
        labels.forEach(span -> table.addCell(textCell(span.text(), BOLD, LABEL_FONT_SIZE, span.colspan(),
                LABEL_ROW_HEIGHT, Element.ALIGN_MIDDLE)));
    }

    private void addValueRow(PdfPTable table, List<Span> values, float rowHeight, int verticalAlignment) {
        values.forEach(span -> table.addCell(textCell(span.text(), REGULAR, VALUE_FONT_SIZE, span.colspan(),
                rowHeight, verticalAlignment)));
    }

    /**
     * Judges are printed {@value #JUDGES_PER_ROW} per row. The booklet always has a "juez principal" box, so
     * the first cell always carries that label: it holds the judge flagged as main in the database, or, when
     * none is flagged, whichever judge comes first. The tail of the last row is padded with empty judge cells,
     * because a row whose colspans do not add up to the grid width is silently dropped by OpenPDF.
     */
    private void addJudgeRows(PdfPTable table, List<EventProofData.Judge> judges, float rowHeight) {
        int printed = 0;
        for (EventProofData.Judge judge : mainJudgeFirst(judges)) {
            table.addCell(judgeCell(judgeLabel(printed), judge.name(), rowHeight));
            printed++;
        }
        while (printed == 0 || printed % JUDGES_PER_ROW != 0) {
            table.addCell(judgeCell(judgeLabel(printed), "", rowHeight));
            printed++;
        }
    }

    private String judgeLabel(int printed) {
        return translate(printed == 0 ? "proof.label.main_judge" : "proof.label.judge");
    }

    /** The judge flagged as main leads; everyone else keeps the order the event gave them. */
    private List<EventProofData.Judge> mainJudgeFirst(List<EventProofData.Judge> judges) {
        List<EventProofData.Judge> ordered = new ArrayList<>(judges.size());
        judges.stream().filter(EventProofData.Judge::mainJudge).forEach(ordered::add);
        judges.stream().filter(judge -> !judge.mainJudge()).forEach(ordered::add);
        return ordered;
    }

    private void addObservationsRows(PdfPTable table, EventProofData proof) {
        table.addCell(textCell(translate("proof.label.observations"), BOLD, LABEL_FONT_SIZE,
                GRID_COLUMNS, LABEL_ROW_HEIGHT, Element.ALIGN_MIDDLE));
        table.addCell(textCell(observations(proof), REGULAR, OBSERVATIONS_FONT_SIZE,
                GRID_COLUMNS, OBSERVATIONS_ROW_HEIGHT, Element.ALIGN_MIDDLE));
    }

    private PdfPCell textCell(String text, BaseFont face, float fontSize, int colspan, float minHeight,
                              int verticalAlignment) {
        PdfPCell cell = new PdfPCell(fitted(text, face, fontSize, colspan));
        cell.setColspan(colspan);
        cell.setBorderWidth(BORDER_WIDTH);
        cell.setPadding(CELL_PADDING);
        // Minimum, never fixed: a fixed height clips, and on a paper form clipping means a value that simply
        // is not there.
        cell.setMinimumHeight(minHeight);
        cell.setVerticalAlignment(verticalAlignment);
        return cell;
    }

    private PdfPCell judgeCell(String label, String name, float height) {
        PdfPCell cell = new PdfPCell();
        cell.setColspan(JUDGE_SPAN);
        cell.setBorderWidth(BORDER_WIDTH);
        cell.setPadding(CELL_PADDING);
        cell.setMinimumHeight(height);
        // Label and name at the top; whatever is left of the cell is the signature space.
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.addElement(new Paragraph(fitted(label, BOLD, LABEL_FONT_SIZE, JUDGE_SPAN)));
        cell.addElement(new Paragraph(fitted(name, REGULAR, VALUE_FONT_SIZE, JUDGE_SPAN)));
        return cell;
    }

    /**
     * Keeps a value inside its cell in three stages: shrink the font down to
     * {@value #MIN_VALUE_FONT_SIZE} pt so a long organizer name or address stays on one line; below that let
     * the cell wrap, which grows the row, the strip and — since the page height is derived from the measured
     * strip — the page; and finally OpenPDF splits a single unbreakable token at character level, so nothing
     * can bleed sideways out of the box.
     */
    private Phrase fitted(String text, BaseFont face, float maxSize, int colspan) {
        String value = nullToEmpty(text);
        float available = availableWidth(colspan);
        float size = maxSize;
        while (size > MIN_VALUE_FONT_SIZE && face.getWidthPoint(value, size) > available) {
            size -= FONT_STEP;
        }
        return new Phrase(value, new Font(face, size));
    }

    private float availableWidth(int colspan) {
        return BLOCK_WIDTH * colspan / GRID_COLUMNS - 2 * CELL_PADDING - BORDER_WIDTH;
    }

    private String observations(EventProofData proof) {
        return OBSERVATIONS_FORMAT.formatted(
                proof.position() == null ? "" : proof.position(),
                nullToEmpty(proof.handler()),
                nullToEmpty(proof.dogName()));
    }

    /**
     * The stage start as a UTC day. Rendering it in the reader's zone would move the printed date of an event
     * that started at midnight, and the booklet is a permanent record.
     */
    private String formatDate(Long epochMillis) {
        return epochMillis == null ? ""
                : PROOF_DATE.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC));
    }

    private static int ceilDiv(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Same contract as the workbook export: the key is its own default, so a missing translation degrades to a
     * visible key instead of an exception.
     */
    private String translate(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private static BaseFont loadFont(String resource) {
        try (InputStream in = EventProofPdfWriter.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing PDF font resource " + resource);
            }
            return BaseFont.createFont(resource, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false,
                    in.readAllBytes(), null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (DocumentException e) {
            throw new IllegalStateException("Corrupt PDF font resource " + resource, e);
        }
    }

    /** A cell's text and how many grid columns it spans, so a row reads as data instead of as five setters. */
    private record Span(String text, int colspan) {
    }
}
