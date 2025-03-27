package com.ccf.sercurity.message;

import com.ccf.sercurity.service.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class LogMessageListener {
    private final LogService logService;

    @KafkaListener(topics = "log_security")
    public void listen(String message) throws JsonProcessingException {
        logService.createLog(message);
    }


}
