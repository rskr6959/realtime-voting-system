package com.project.subscriber;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GlobalEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GlobalEventListener.class);

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        logger.info("Context refreshed event received.");
    }
} 