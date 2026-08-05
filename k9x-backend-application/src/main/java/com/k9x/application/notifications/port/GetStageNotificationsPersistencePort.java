package com.k9x.application.notifications.port;

import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;

import java.util.List;
import java.util.Map;

public interface GetStageNotificationsPersistencePort {

    /**
     * The announcements of each requested stage, newest first, keyed by stage id. Stages without
     * announcements are absent from the map rather than mapped to an empty list.
     *
     * <p>Announcements are read through their own port instead of being hydrated into the competition
     * aggregate: they carry none of its invariants and are written outside it.
     */
    Map<String, List<StageNotificationDTO>> getByStageIds(List<String> stageIds);
}
