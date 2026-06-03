package com.k9x.infrastructure.configuration.geo;

import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.infrastructure.out.rest.geo.NominatimGeoCoordinatesAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoAdapterConfiguration {

    @Bean
    public GeoCoordinatesPort geoCoordinatesPort(
            @Value("${k9x-backend.geo.nominatim.user-agent}") String userAgent
    ) {
        return new NominatimGeoCoordinatesAdapter(userAgent);
    }
}
