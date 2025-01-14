package com.scrooge.web.dto;

import com.scrooge.model.enums.Country;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Currency;

@Data
@Builder
public class WalletCreateRequest {

    @NotNull
    private String name;
    @NotNull
    private Currency currency;
    private Country country;
}
