package com.aston.handson.cloudbus.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ConfigController {

    @Value("${custom.config.example:default-value}")
    private String exampleConfig;

    @GetMapping("/config/example")
    public String getExampleConfig() {
        return exampleConfig;
    }
}
