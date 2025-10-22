package com.scrooge.web.dto.wallet;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreateRequest {

    @NotBlank(message = "Wallet name is required")
    private String name;

    @NotBlank(message = "Currency is required")
    private String currency;
}
