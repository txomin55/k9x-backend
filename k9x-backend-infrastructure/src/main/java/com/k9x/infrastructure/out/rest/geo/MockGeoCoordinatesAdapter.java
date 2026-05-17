package com.k9x.infrastructure.out.rest.geo;

import com.k9x.application.competitions.dto.Coordinates;
import com.k9x.application.competitions.port.GeoCoordinatesPort;

public class MockGeoCoordinatesAdapter implements GeoCoordinatesPort {

    // TODO: replace with a real geocoding service implementation
    @Override
    public Coordinates getCoordinates(String address) {
        return new Coordinates(null, null);
    }
}
