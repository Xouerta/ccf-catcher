package com.ccf.sercurity.message;

import com.ccf.sercurity.service.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LogMessageListener {
    private final LogService logService;

    @KafkaListener(topics = "log_security")
    public void listen(String message) throws JsonProcessingException {
        logService.createLog(message);
    }


}
