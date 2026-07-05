package com.k9x.infrastructure.in.rest.mapper;

import com.k9x.oas.stub.model.IdNameDTO;

import java.util.List;

public final class AwardResponseMapper {

    private AwardResponseMapper() {
    }

    public static List<IdNameDTO> toIdNameList(List<String> awardIds) {
        if (awardIds == null) {
            return List.of();
        }
        return awardIds.stream()
                .map(id -> new IdNameDTO(capitalize(id), id))
                .toList();
    }

    private static String capitalize(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }
}
