package com.scrooge.web.dto;

import com.scrooge.model.enums.Country;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateRequest {

    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @Email
    private String email;
    @NotNull
    private String phoneNumber;
    @NotNull
    private Country country;
    @NotNull
    private String password;
}
