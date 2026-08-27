package com.k9x.application.dogs.use_case.dto;

import com.k9x.application.dogs.port.payload.DogListFilter;

import java.util.List;

/**
 * A page of dogs. When no page size was requested the whole list comes back as a single page, so
 * {@code size} equals the number of returned items.
 */
public record DogListDTO(List<DogDTO> items, int page, int size, long total, int totalPages) {

    public static DogListDTO of(List<DogDTO> items, DogListFilter filter, long total) {
        int size = filter.paginated() ? filter.limit() : items.size();
        int totalPages = total == 0 || size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new DogListDTO(items, filter.page(), size, total, totalPages);
    }
}
