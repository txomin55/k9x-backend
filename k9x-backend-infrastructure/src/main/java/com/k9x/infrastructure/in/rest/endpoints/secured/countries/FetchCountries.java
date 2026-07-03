package com.k9x.infrastructure.in.rest.endpoints.secured.countries;

import com.k9x.application.countries.use_case.GetCountryListServiceCase;
import com.k9x.oas.stub.api.SecuredCountriesFetchAllApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchCountries implements SecuredCountriesFetchAllApiDelegate {

    private final GetCountryListServiceCase getCountryListServiceCase;

    public FetchCountries(GetCountryListServiceCase getCountryListServiceCase) {
        this.getCountryListServiceCase = getCountryListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchCountriesSecured() {
        return ResponseEntity.ok(
                getCountryListServiceCase.getCountries().stream()
                        .map(country -> new IdNameDTO(country.name(), country.id()))
                        .toList()
        );
    }
}
