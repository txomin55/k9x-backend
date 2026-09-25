package com.k9x.infrastructure.in.rest.endpoints.countries;

import com.k9x.application.countries.use_case.GetCountryListServiceCase;
import com.k9x.oas.stub.api.CountriesFetchAllApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * The same country list as the secured endpoint, without a token: the public lists offer every country in their
 * country filter, and an anonymous visitor has none.
 */
public class FetchPublicCountries implements CountriesFetchAllApiDelegate {

    private final GetCountryListServiceCase getCountryListServiceCase;

    public FetchPublicCountries(GetCountryListServiceCase getCountryListServiceCase) {
        this.getCountryListServiceCase = getCountryListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchCountries() {
        return ResponseEntity.ok(
                getCountryListServiceCase.getCountries().stream()
                        .map(country -> new IdNameDTO(country.name(), country.id()))
                        .toList()
        );
    }
}
