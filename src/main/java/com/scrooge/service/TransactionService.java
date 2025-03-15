package com.scrooge.service;

import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.Transaction;
import com.scrooge.model.User;
import com.scrooge.model.enums.TransactionType;
import com.scrooge.model.Wallet;
import com.scrooge.web.mapper.TransactionMapper;
import com.scrooge.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public Transaction getTransactionById(UUID transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Transaction ID - %s".formatted(transactionId)));
    }

    public void setTransactionToWallet(Wallet wallet, BigDecimal amount, TransactionType type, boolean successful) {

        Transaction transaction = TransactionMapper.mapToTransaction(amount, type, successful);
        transaction.setWallet(wallet);

        transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactionsByUserId(UUID userId) {

        User user = userService.getUserById(userId);

        List<Wallet> wallets = user.getWallets();

        return wallets.stream()
                .flatMap(wallet -> wallet.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedOn).reversed())
                .collect(Collectors.toList());
    }

    public void delete(Transaction transaction) {

        transactionRepository.delete(transaction);
    }
}
