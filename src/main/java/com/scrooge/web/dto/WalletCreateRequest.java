package com.scrooge.web.dto;

import com.scrooge.model.enums.Country;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreateRequest {

    @NotNull
    private String name;
    @NotNull
    private String currency;
}
