package com.k9x.application.users.port;

import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;

import java.util.List;

public interface GetPushSubscriptionsPersistencePort {

    List<PushSubscriptionTargetDTO> getByUserId(String userId);
}
