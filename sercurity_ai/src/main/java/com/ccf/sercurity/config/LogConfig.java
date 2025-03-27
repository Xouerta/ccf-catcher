package com.ccf.sercurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "log.api")
public class LogConfig {
    private String url;
    private String start;
    private String end;

    public String getStartUrl() {
        return url + start;
    }

    public String getEndUrl() {
        return url + end;
    }
}
