package com.k9x.infrastructure.out.enums.awards;

import com.k9x.application.awards.port.GetAwardListPort;
import com.k9x.application.awards.use_case.dto.AwardDTO;
import com.k9x.domain.disciplines.valueobjects.Discipline;

import java.util.Arrays;
import java.util.List;

public class AwardEnumAdapter implements GetAwardListPort {

    @Override
    public List<AwardDTO> getAwards(String disciplineId) {
        Discipline discipline = Discipline.fromRequest(disciplineId);
        return switch (discipline) {
            case OBDX -> Arrays.stream(ObdxAward.values())
                    .map(award -> new AwardDTO(award.name(), capitalize(award.name())))
                    .toList();
        };
    }

    private String capitalize(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }
}
