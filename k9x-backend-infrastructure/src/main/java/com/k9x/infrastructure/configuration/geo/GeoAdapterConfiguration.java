package com.k9x.infrastructure.configuration.geo;

import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.infrastructure.out.rest.geo.MockGeoCoordinatesAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoAdapterConfiguration {

    @Bean
    public GeoCoordinatesPort geoCoordinatesPort() {
        return new MockGeoCoordinatesAdapter();
    }
}
