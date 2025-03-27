package com.ccf.sercurity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSendConfig {
    private final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    private final JavaMailSender mailSender;
    private final MailConfig config;

    @Async
    public void send(String to, String title, String text) {
        simpleMailMessage.setFrom(config.getUsername());
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(title);
        simpleMailMessage.setText(text);
        mailSender.send(simpleMailMessage);
    }
}
