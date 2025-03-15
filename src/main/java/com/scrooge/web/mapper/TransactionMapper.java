package com.scrooge.web.mapper;

import com.scrooge.model.Transaction;
import com.scrooge.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionMapper {

    public static Transaction mapToTransaction(BigDecimal amount, TransactionType type, boolean successful) {

        return Transaction.builder()
                .amount(amount)
                .type(type)
                .successful(successful)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
