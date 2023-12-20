package com.gmonitor.configurations;

import com.gmonitor.service.RunConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    private final RunConfigurationService runConfigurationService;

    WebSocketEventListener(RunConfigurationService runConfigurationService) {
        this.runConfigurationService = runConfigurationService;
    }


    @EventListener
    public void subscriptionEventListener(SessionSubscribeEvent event) {
        log.info("Received subscription request");
        runConfigurationService.sendLastMessage();
    }

}
