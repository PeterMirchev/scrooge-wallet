package com.scrooge.web.dto.pocket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@Builder
public class PocketUpdateRequest {

    @NotNull
    private String name;
    @NotNull
    private String goalDescription;
    @NotNull
    @Positive
    private BigDecimal targetAmount;
    @NotNull
    private Currency currency;
}
