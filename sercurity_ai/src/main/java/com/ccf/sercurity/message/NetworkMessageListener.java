package com.ccf.sercurity.message;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NetworkMessageListener {
    @KafkaListener(topics = "network_predictions")
    public void listen(String message) {
        System.out.println("Received Messasge in group network: " + message);
    }
}
