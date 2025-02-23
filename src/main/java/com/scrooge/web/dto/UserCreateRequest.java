package com.scrooge.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @Email
    private String email;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "passwords must match")
    public boolean isMatchingPasswords() {

        return password != null && password.equals(confirmPassword);
    }
}
