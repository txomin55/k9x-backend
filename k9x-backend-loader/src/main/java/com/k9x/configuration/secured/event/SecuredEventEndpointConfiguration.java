package com.k9x.configuration.secured.event;

import com.k9x.infrastructure.in.rest.endpoints.secured.event.CreateEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.EnrollEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.FetchAllByStagesEventData;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.RemoveEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.UpdateObdxEventInfo;
import com.k9x.infrastructure.in.rest.endpoints.secured.event.UpdateObdxScore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventEndpointConfiguration {

    @Bean
    public CreateEvent createEvent() {
        return new CreateEvent();
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
    public RemoveEvent removeEvent() {
        return new RemoveEvent();
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
