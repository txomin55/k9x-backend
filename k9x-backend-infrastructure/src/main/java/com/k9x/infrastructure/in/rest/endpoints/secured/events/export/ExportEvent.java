package com.k9x.infrastructure.in.rest.endpoints.secured.events.export;

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

import java.nio.charset.StandardCharsets;

public class ExportEvent implements SecuredEventsExportApiDelegate {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final String DEFAULT_FILE_NAME = "event";

    private final GetEventServiceCase getEventServiceCase;
    private final UserInfoDTO userDetails;
    private final EventWorkbookWriter eventWorkbookWriter;

    public ExportEvent(GetEventServiceCase getEventServiceCase, UserInfoDTO userDetails,
                       EventWorkbookWriter eventWorkbookWriter) {
        this.getEventServiceCase = getEventServiceCase;
        this.userDetails = userDetails;
        this.eventWorkbookWriter = eventWorkbookWriter;
    }

    @Override
    public ResponseEntity<Resource> exportEventSecured(String eventId, Boolean includeClassification) {
        // includeClassification is accepted but not honoured yet: the workbook has no classification sheet, so the
        // CPU-heavy classification aggregation is deliberately not triggered.
        FetchEventDetailDTO event = getEventServiceCase.getEvent(eventId, userDetails.getEmail(),
                userDetails.isOrganizer());

        byte[] workbook = eventWorkbookWriter.write(event);

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
