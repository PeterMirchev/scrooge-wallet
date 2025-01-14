package com.scrooge.web.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestLogin {

    @NotNull
    private String email;
    @Size(min = 6)
    private String password;
}
