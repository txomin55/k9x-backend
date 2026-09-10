package com.k9x.application.dogs.use_case.dto;

import com.k9x.application.dogs.port.payload.PublicDogListFilter;

import java.util.List;

/**
 * A page of public dogs. When no page size was requested the whole list comes back as a single page, so
 * {@code size} equals the number of returned items.
 */
public record PublicDogListDTO(List<PublicDogDTO> items, int page, int size, long total, int totalPages) {

    public static PublicDogListDTO of(List<PublicDogDTO> items, PublicDogListFilter filter, long total) {
        int size = filter.paginated() ? filter.limit() : items.size();
        int totalPages = total == 0 || size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PublicDogListDTO(items, filter.page(), size, total, totalPages);
    }
}
