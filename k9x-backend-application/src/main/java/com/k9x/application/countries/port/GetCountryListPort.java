package com.k9x.application.countries.port;

import com.k9x.application.countries.use_case.dto.CountryDTO;

import java.util.List;

public interface GetCountryListPort {

    List<CountryDTO> getCountries();
}
