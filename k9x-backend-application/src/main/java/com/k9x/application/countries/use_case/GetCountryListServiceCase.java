package com.k9x.application.countries.use_case;

import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.application.countries.use_case.dto.CountryDTO;

import java.util.List;

public class GetCountryListServiceCase {

    private final GetCountryListPort getCountryListPort;

    public GetCountryListServiceCase(GetCountryListPort getCountryListPort) {
        this.getCountryListPort = getCountryListPort;
    }

    public List<CountryDTO> getCountries() {
        return getCountryListPort.getCountries();
    }
}
