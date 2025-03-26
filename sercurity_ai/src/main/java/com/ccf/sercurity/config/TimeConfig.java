package com.ccf.sercurity.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimeConfig {
    @PostConstruct
    void started() {
        // 东八区
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }
}
