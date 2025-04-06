package com.ccf.sercurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepstudylog.api")
public class DeepStudyLogConfig {
    private String url;
    private String processLog;
    private String evaluate;

    public String getProcessLogUrl() {
        return url + processLog;
    }

    public String getEvaluateUrl() {
        return url + evaluate;
    }
}
