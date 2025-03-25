package com.scrooge.service;

import com.scrooge.TestBuilder;
import com.scrooge.model.Transaction;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getAllTransactionsByUserId_happyPath() {

        User user = TestBuilder.aRandomUser();
        Wallet wallet = user.getWallets().get(0);
        Transaction transaction1 = wallet.getTransactions().get(0);
        Transaction transaction2 = wallet.getTransactions().get(1);

        wallet.setTransactions(Arrays.asList(transaction1, transaction2));
        user.setWallets(Collections.singletonList(wallet));

        when(userService.getUserById(user.getId())).thenReturn(user);

        List<Transaction> transactions = transactionService.getAllTransactionsByUserId(user.getId());

        assertEquals(2, transactions.size());
    }

}