package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;

import java.util.List;

public interface UpdateObdxCompetitorPositionsPersistencePort {
    void updatePositions(String eventId, List<ObdxCompetitorPosition> positions);
}
