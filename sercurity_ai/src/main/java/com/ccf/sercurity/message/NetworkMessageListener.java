package com.ccf.sercurity.message;

import com.ccf.sercurity.service.TrafficAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkMessageListener {

    private final TrafficAnalysisService trafficAnalysisService;

    @KafkaListener(topics = "network_predictions")
    public void listen(String message) throws Exception {
        trafficAnalysisService.saveTrafficData(message);
    }
}
