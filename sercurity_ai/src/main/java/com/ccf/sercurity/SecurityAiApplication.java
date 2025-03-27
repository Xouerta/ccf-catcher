package com.ccf.sercurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableElasticsearchRepositories
public class SecurityAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityAiApplication.class, args);
    }
}
