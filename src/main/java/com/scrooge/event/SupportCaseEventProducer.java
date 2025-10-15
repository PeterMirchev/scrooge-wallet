package com.scrooge.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SupportCaseEventProducer {

    private final KafkaTemplate<String, SupportCaseEvent> kafkaTemplate;

    @Value("${topic.support.case}")
    private String topicName;

    public SupportCaseEventProducer(KafkaTemplate<String, SupportCaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSupportCase(SupportCaseEvent event) {
        kafkaTemplate.send(topicName, event);
        System.out.println("📤 Sent support case event: " + event);
    }
}
