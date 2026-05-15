package com.k9x.application.users.port;

import com.k9x.application.users.dto.UserInfoDTO;

public interface GetUserInfoPersistencePort {

    UserInfoDTO findById(String id);
}
