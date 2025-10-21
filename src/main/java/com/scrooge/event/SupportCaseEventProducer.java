package com.scrooge.event;

import com.scrooge.event.dto.SupportCaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SupportCaseEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SupportCaseEventProducer.class);

    private final KafkaTemplate<String, SupportCaseEvent> kafkaTemplate;

    @Value("${topic.support.case}")
    private String topicName;

    public SupportCaseEventProducer(KafkaTemplate<String, SupportCaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSupportCase(SupportCaseEvent event) {

        kafkaTemplate.send(topicName, event);
        log.info("Sent support case event: {}", event);
    }
}
