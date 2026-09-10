package com.k9x.application.dogs.port.payload;

import com.k9x.application.dogs.use_case.dto.FetchPublicDogDTO;

import java.util.List;

/**
 * The public dogs of the requested page together with how many dogs match the filter across every page.
 */
public record PublicDogListPage(List<FetchPublicDogDTO> dogs, long total) {
}
