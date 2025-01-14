package com.scrooge.web.mapper;

import com.scrooge.model.Wallet;
import com.scrooge.web.dto.WalletCreateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletMapper {

    public static Wallet mapToWallet(WalletCreateRequest request) {

        return Wallet.builder()
                .name(request.getName())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
