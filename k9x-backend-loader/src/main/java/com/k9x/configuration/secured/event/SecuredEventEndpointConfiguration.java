package com.k9x.configuration.secured.event;

import com.k9x.application.events.obdx.use_case.CreateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.DeleteObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.EnrollObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.EnrollEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.FetchAllByStagesEventData;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.CreateObdxEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.RemoveObdxEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.UpdateObdxEventInfo;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.UpdateObdxScore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventEndpointConfiguration {

    @Bean
    public CreateObdxEvent createEvent(CreateObdxEventServiceCase createObdxEventServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateObdxEvent(createObdxEventServiceCase, userInfoDTO);
    }

    @Bean
    public EnrollEvent enrollEvent(EnrollObdxEventServiceCase enrollObdxEventServiceCase) {
        return new EnrollEvent(enrollObdxEventServiceCase);
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
    public UpdateObdxEventInfo updateObdxEventInfo(UpdateObdxEventServiceCase updateObdxEventServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateObdxEventInfo(updateObdxEventServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateObdxScore updateObdxScore() {
        return new UpdateObdxScore();
    }
}
