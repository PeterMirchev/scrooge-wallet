package com.scrooge.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupportRequest {

    @Size(min = 10, message = "Minimum 10 characters of message is required.")
    private String message;
}
