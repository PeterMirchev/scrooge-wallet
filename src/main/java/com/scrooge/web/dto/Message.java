package com.scrooge.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Message {

    private UUID id;
    private String text;
    private String author;
    private LocalDateTime dateTime;
}
