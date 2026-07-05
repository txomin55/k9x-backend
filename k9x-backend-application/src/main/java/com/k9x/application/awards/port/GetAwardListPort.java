package com.k9x.application.awards.port;

import com.k9x.application.awards.use_case.dto.AwardDTO;

import java.util.List;

public interface GetAwardListPort {

    List<AwardDTO> getAwards(String disciplineId);
}
