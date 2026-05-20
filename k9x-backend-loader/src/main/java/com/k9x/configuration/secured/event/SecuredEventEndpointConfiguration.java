package com.k9x.configuration.secured.event;

import com.k9x.application.events.obdx.use_case.CreateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.DeleteObdxEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.EnrollEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.FetchAllByStagesEventData;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.obdx.CreateObdxEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.obdx.RemoveObdxEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.obdx.UpdateObdxEventInfo;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.obdx.UpdateObdxScore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventEndpointConfiguration {

    @Bean
    public CreateObdxEvent createEvent(CreateObdxEventServiceCase createObdxEventServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateObdxEvent(createObdxEventServiceCase, userInfoDTO);
    }

    @Bean
    public EnrollEvent enrollEvent() {
        return new EnrollEvent();
    }

    @Bean
    public FetchAllByStagesEventData fetchAllByStagesEventData() {
        return new FetchAllByStagesEventData();
    }

    @Bean
    public RemoveObdxEvent removeEvent(DeleteObdxEventServiceCase deleteObdxEventServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveObdxEvent(deleteObdxEventServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateObdxEventInfo updateObdxEventInfo() {
        return new UpdateObdxEventInfo();
    }

    @Bean
    public UpdateObdxScore updateObdxScore() {
        return new UpdateObdxScore();
    }
}
