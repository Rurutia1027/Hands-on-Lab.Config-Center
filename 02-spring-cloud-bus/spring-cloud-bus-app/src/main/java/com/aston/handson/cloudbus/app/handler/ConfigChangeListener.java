package com.aston.handson.cloudbus.app.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class ConfigChangeListener implements ApplicationListener<EnvironmentChangeEvent> {
    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changedKeys = event.getKeys();
        log.info("Detected configuration changes: {}", changedKeys);
    }
}