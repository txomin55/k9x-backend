package com.k9x.configuration.secured.event;

import com.k9x.application.events.obdx.use_case.*;
import com.k9x.application.events.use_case.CreateEventServiceCase;
import com.k9x.application.events.use_case.DeleteEventServiceCase;
import com.k9x.application.events.use_case.EnrollEventServiceCase;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.CreateEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.EnrollEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.GetEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.RemoveEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.export.EventWorkbookWriter;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.export.ExportEvent;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.UpdateObdxEventInfo;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.UpdateObdxEventNotCompeting;
import com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx.UpdateObdxScore;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventEndpointConfiguration {

    @Bean
    public CreateEvent createEvent(CreateEventServiceCase createEventServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateEvent(createEventServiceCase, userInfoDTO);
    }

    @Bean
    public EnrollEvent enrollEvent(EnrollEventServiceCase enrollEventServiceCase, UserInfoDTO userInfoDTO) {
        return new EnrollEvent(enrollEventServiceCase, userInfoDTO);
    }

    @Bean
    public GetEvent getEvent(GetEventServiceCase getEventServiceCase, UserInfoDTO userInfoDTO, MessageSource messageSource) {
        return new GetEvent(getEventServiceCase, userInfoDTO, messageSource);
    }

    @Bean
    public EventWorkbookWriter eventWorkbookWriter(MessageSource messageSource) {
        return new EventWorkbookWriter(messageSource);
    }

    @Bean
    public ExportEvent exportEvent(GetEventServiceCase getEventServiceCase,
                                   GetEventClassificationServiceCase getEventClassificationServiceCase,
                                   GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                   UserInfoDTO userInfoDTO, EventWorkbookWriter eventWorkbookWriter) {
        return new ExportEvent(getEventServiceCase, getEventClassificationServiceCase,
                getObdxClassificationConfigPort, userInfoDTO, eventWorkbookWriter);
    }

    @Bean
    public RemoveEvent removeEvent(DeleteEventServiceCase deleteEventServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveEvent(deleteEventServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateObdxEventInfo updateObdxEventInfo(UpdateObdxEventServiceCase updateObdxEventServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateObdxEventInfo(updateObdxEventServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateObdxScore updateObdxScore(UpdateObdxScoreServiceCase updateObdxScoreServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateObdxScore(updateObdxScoreServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateObdxEventNotCompeting updateNotCompeting(UpdateNotCompetingServiceCase updateNotCompetingServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateObdxEventNotCompeting(updateNotCompetingServiceCase, userInfoDTO);
    }

}
