package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsExportApiDelegate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ExportEvent implements SecuredEventsExportApiDelegate {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final String DEFAULT_FILE_NAME = "event";

    private final GetEventServiceCase getEventServiceCase;
    private final GetEventClassificationServiceCase getEventClassificationServiceCase;
    private final UserInfoDTO userDetails;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final EventWorkbookWriter eventWorkbookWriter;

    public ExportEvent(GetEventServiceCase getEventServiceCase,
                       GetEventClassificationServiceCase getEventClassificationServiceCase,
                       GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                       UserInfoDTO userDetails, EventWorkbookWriter eventWorkbookWriter) {
        this.getEventServiceCase = getEventServiceCase;
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
        this.userDetails = userDetails;
        this.eventWorkbookWriter = eventWorkbookWriter;
    }

    @Override
    public ResponseEntity<Resource> exportEventSecured(String eventId, Boolean includeClassification) {
        FetchEventDetailDTO event = getEventServiceCase.getEvent(eventId, userDetails.getEmail(),
                userDetails.isOrganizer());

        // Reuses the classification use case rather than re-aggregating here, so the export inherits its cache
        // and daily snapshot instead of paying the full CPU-heavy aggregation on every download.
        FetchClassificationDTO classification = Boolean.TRUE.equals(includeClassification)
                ? getEventClassificationServiceCase.getClassification(eventId)
                : null;

        // The exercise coefficients live in the discipline configuration, not in the classification, and the
        // per-competitor sheets need them to show the judges' average beside the weighted points.
        Map<String, BigDecimal> coefByExerciseId = classification == null || event.configuration() == null
                ? Map.of()
                : getObdxClassificationConfigPort.getConfig(event.configuration().id()).coefByExerciseId();

        byte[] workbook = eventWorkbookWriter.write(event, classification, coefByExerciseId);

        return ResponseEntity.ok()
                .contentType(XLSX)
                .contentLength(workbook.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName(event), StandardCharsets.UTF_8)
                                .build().toString())
                .body(new ByteArrayResource(workbook));
    }

    /**
     * The download is named after the event, spaces and accents included. Encoding it as UTF-8 makes Spring
     * emit the RFC 5987 {@code filename*} form alongside an ASCII fallback, so "Copa de Primavera" survives
     * instead of arriving mangled. Only the characters no filesystem accepts are replaced.
     */
    private String fileName(FetchEventDetailDTO event) {
        String name = event.obdx() == null ? null : event.obdx().name();
        if (name == null || name.isBlank()) {
            return DEFAULT_FILE_NAME + ".xlsx";
        }
        return name.trim().replaceAll("[/\\\\:*?\"<>|\\p{Cntrl}]", "_") + ".xlsx";
    }
}
