package com.scrooge.web.dto;

import com.scrooge.model.enums.Country;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String country;
}

