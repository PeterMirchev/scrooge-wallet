package com.scrooge.web.dto.pocket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PocketCreateRequest {

    @NotNull
    private String name;
    @NotNull
    private String goalDescription;
    @NotNull
    @Positive
    private BigDecimal targetAmount;
    @NotNull
    private Currency currency;

    public String getName() {
        return name;
    }

}
