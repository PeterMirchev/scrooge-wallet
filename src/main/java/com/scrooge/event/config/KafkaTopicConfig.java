package com.scrooge.event.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.case-create}")
    private String supportCaseTopic;
    @Value("${kafka.topic.case-update}")
    private String caseUpdateTopic;
    @Value("${kafka.topic.case-message}")
    private String messageSendTopic;

    @Bean
    public NewTopic caseCreateTopic() {

        return TopicBuilder.name(supportCaseTopic)
                .build();
    }

    @Bean
    public NewTopic caseUpdateTopic() {

        return TopicBuilder.name(caseUpdateTopic)
                .build();
    }

    @Bean
    public NewTopic caseMessageTopic() {

        return TopicBuilder.name(messageSendTopic)
                .build();
    }
}
