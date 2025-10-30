package com.scrooge.event;

import com.scrooge.event.dto.CaseMessageRequest;
import com.scrooge.event.dto.SupportCaseEvent;
import com.scrooge.event.dto.CaseUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SupportCaseEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SupportCaseEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.case-create}")
    private String caseCreateTopic;

    @Value("${kafka.topic.case-update}")
    private String caseUpdateTopic;

    @Value("${kafka.topic.case-message}")
    private String caseMessageTopic;

    public SupportCaseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSupportCase(SupportCaseEvent event) {

        kafkaTemplate.send(caseCreateTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent support case event to {}: {}", caseCreateTopic, event);
                    }
                    else {
                        log.error("Failed to send support case event: {}", event, ex);
                    }
                });
    }

    public void sendUpdateEvent(CaseUpdateRequest event) {

        kafkaTemplate.send(caseUpdateTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent case update event to {}: {}", caseUpdateTopic, event);
                    }
                    else {
                        log.error("Failed to send case update event: {}", event, ex);
                    }
                });
    }

    public void sendMessageEvent(CaseMessageRequest event) {

        kafkaTemplate.send(caseMessageTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent message event to {}: {}", caseMessageTopic, event);
                    }
                    else {
                        log.error("Failed to send message event: {}", event, ex);
                    }
                });
    }
}
