package com.scrooge.service;

import com.scrooge.event.dto.SupportCaseEvent;
import com.scrooge.event.SupportCaseEventProducer;
import com.scrooge.model.User;
import com.scrooge.web.dto.cases.SupportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportService.class);

    private final SupportCaseEventProducer eventProducer;

    public SupportService(SupportCaseEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void sendMessageToSupport(User user, SupportRequest request) {

        if (user == null || request == null) {
            throw new IllegalArgumentException("User and SupportRequest cannot be null");
        }

        SupportCaseEvent event = SupportCaseEvent.builder()
                .requesterId(user.getId())
                .requesterName(user.getFirstName() + " " + user.getLastName())
                .requesterEmail(user.getEmail())
                .description(request.getMessage())
                .build();

        log.info("📤 Sending support event for user {} [{}]", user.getEmail(), user.getId());

        eventProducer.sendSupportCase(event);
    }
}
