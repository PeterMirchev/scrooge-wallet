package com.scrooge.web.mapper;

import com.scrooge.model.Wallet;
import com.scrooge.web.dto.wallet.WalletCreateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

public class WalletMapper {

    public static Wallet mapToWallet(WalletCreateRequest request) {

        return Wallet.builder()
                .name(request.getName())
                .currency(Currency.getInstance(request.getCurrency()))
                .balance(BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
