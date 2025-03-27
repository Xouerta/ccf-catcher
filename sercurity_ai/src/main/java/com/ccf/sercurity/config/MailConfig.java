package com.ccf.sercurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String protocol;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(this.getHost());
        javaMailSender.setPort(this.getPort());
        javaMailSender.setUsername(this.getUsername());
        javaMailSender.setPassword(this.getPassword());
        javaMailSender.setProtocol(this.getProtocol());
        return javaMailSender;
    }

}