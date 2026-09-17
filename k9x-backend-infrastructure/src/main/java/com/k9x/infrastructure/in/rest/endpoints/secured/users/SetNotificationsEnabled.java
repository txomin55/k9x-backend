package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.application.users.use_case.SetNotificationsEnabledServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserNotificationsEnabledApiDelegate;
import com.k9x.oas.stub.model.NotificationsEnabledRequestDTO;
import org.springframework.http.ResponseEntity;

public class SetNotificationsEnabled implements SecuredUserNotificationsEnabledApiDelegate {

    private final SetNotificationsEnabledServiceCase setNotificationsEnabledServiceCase;
    private final UserInfoDTO userDetails;

    public SetNotificationsEnabled(SetNotificationsEnabledServiceCase setNotificationsEnabledServiceCase,
                                   UserInfoDTO userDetails) {
        this.setNotificationsEnabledServiceCase = setNotificationsEnabledServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> setNotificationsEnabledSecured(NotificationsEnabledRequestDTO body) {
        setNotificationsEnabledServiceCase.setNotificationsEnabled(
                userDetails.getEmail(), Boolean.TRUE.equals(body.getEnabled()));
        return ResponseEntity.ok().build();
    }
}
