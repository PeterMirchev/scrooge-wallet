package com.scrooge.service;

import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.Transaction;
import com.scrooge.model.enums.TransactionType;
import com.scrooge.model.Wallet;
import com.scrooge.web.mapper.TransactionMapper;
import com.scrooge.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction getTransactionById(UUID transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Transaction ID - %s".formatted(transactionId)));
    }

    public void setTransactionToWallet(Wallet wallet, BigDecimal amount, TransactionType type) {

        Transaction transaction = TransactionMapper.mapToTransaction(amount, type);
        transaction.setWallet(wallet);

        transactionRepository.save(transaction);
    }
}
