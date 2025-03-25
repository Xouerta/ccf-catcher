package com.ccf.sercurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories
public class SercurityAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SercurityAiApplication.class, args);
    }

}
