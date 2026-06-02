package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.use_cases.dto.ObdxClassificationConfigDTO;

public interface GetObdxClassificationConfigPort {

    ObdxClassificationConfigDTO getConfig(String configurationId);
}
