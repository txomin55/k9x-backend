package com.k9x.application.competitions.port;

import com.k9x.application.competitions.dto.Coordinates;

public interface GeoCoordinatesPort {

    // TODO: implement a real geocoding service that resolves address to coordinates
    Coordinates getCoordinates(String address);
}
