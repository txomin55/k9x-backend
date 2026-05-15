package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.oas.stub.api.SecuredCompetitionsFetchAllApiDelegate;
import com.k9x.oas.stub.model.CompetitionNotificationDetailResponseDTO;
import com.k9x.oas.stub.model.CompetitionResponseDTO;
import com.k9x.oas.stub.model.CompetitionStageDetailResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchCompetitions implements SecuredCompetitionsFetchAllApiDelegate {

    @Override
    public ResponseEntity<List<CompetitionResponseDTO>> fetchCompetitionsSecured() {
        return ResponseEntity.ok(List.of(
                new CompetitionResponseDTO(
                        "comp-1",
                        "Mocked Competition",
                        "Mocked description",
                        "ES",
                        "OPEN",
                        "Calle Mayor 1, Madrid",
                        List.of(
                                new CompetitionStageDetailResponseDTO(1747000000, 1747100000, "stage-1", "Qualifying Round"),
                                new CompetitionStageDetailResponseDTO(1747200000, 1747300000, "stage-2", "Final")
                        ),
                        List.of(
                                new CompetitionNotificationDetailResponseDTO("notif-1", 1747000000L, "Registration is now open"),
                                new CompetitionNotificationDetailResponseDTO("notif-2", 1747050000L, "Reminder: deadline in 3 days")
                        )
                )
        ));
    }
}
