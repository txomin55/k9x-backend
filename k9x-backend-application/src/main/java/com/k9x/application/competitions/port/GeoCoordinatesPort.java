package com.k9x.application.competitions.port;

import com.k9x.application.competitions.use_case.dto.Coordinates;

public interface GeoCoordinatesPort {

    Coordinates getCoordinates(String address);
}
