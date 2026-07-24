package com.k9x.configuration.secured.notifications;

import com.k9x.application.notifications.use_case.GetNotificationListServiceCase;
import com.k9x.application.notifications.use_case.MarkNotificationsSeenServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.notifications.GetNotifications;
import com.k9x.infrastructure.in.rest.endpoints.secured.notifications.MarkNotificationsSeen;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredNotificationsEndpointConfiguration {

    @Bean
    public GetNotifications getNotifications(GetNotificationListServiceCase getNotificationListServiceCase,
                                             UserInfoDTO userInfoDTO) {
        return new GetNotifications(getNotificationListServiceCase, userInfoDTO);
    }

    @Bean
    public MarkNotificationsSeen markNotificationsSeen(MarkNotificationsSeenServiceCase markNotificationsSeenServiceCase,
                                                       UserInfoDTO userInfoDTO) {
        return new MarkNotificationsSeen(markNotificationsSeenServiceCase, userInfoDTO);
    }
}
