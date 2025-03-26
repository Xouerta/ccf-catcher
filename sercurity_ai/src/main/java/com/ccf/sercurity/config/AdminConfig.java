package com.ccf.sercurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    private String email;
    private String password;
    private String username = "admin";
//    private Boolean init = false;
}
