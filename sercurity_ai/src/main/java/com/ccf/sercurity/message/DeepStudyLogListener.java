package com.ccf.sercurity.message;

import com.ccf.sercurity.service.DeepStudyLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeepStudyLogListener {
    private final DeepStudyLogService deepStudyLogService;
    @KafkaListener(topics = "deep_study_log")
    public void listen(String message) throws JsonProcessingException {
        deepStudyLogService.saveDeepStudyLog(message);
    }
}
