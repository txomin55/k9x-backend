package com.k9x.application.awards.use_case;

import com.k9x.application.awards.port.GetAwardListPort;
import com.k9x.application.awards.use_case.dto.AwardDTO;

import java.util.List;

public class GetAwardListServiceCase {

    private final GetAwardListPort getAwardListPort;

    public GetAwardListServiceCase(GetAwardListPort getAwardListPort) {
        this.getAwardListPort = getAwardListPort;
    }

    public List<AwardDTO> getAwards(String disciplineId) {
        return getAwardListPort.getAwards(disciplineId);
    }
}
