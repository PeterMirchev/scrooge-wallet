package com.scrooge.service;

import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.repository.AuditLogRepository;
import com.scrooge.repository.UserRepository;
import com.scrooge.repository.WalletRepository;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.WalletCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WalletServiceITest {

    @Autowired
    private UserService userService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setup() {

        auditLogRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    void whenTransferMoneyByEmail_HappyPath() {

        //given
        UserCreateRequest firstUser = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();

        UserCreateRequest secondUser = UserCreateRequest.builder()
                .email("secondUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();

        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();
        WalletCreateRequest secondUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();

        userService.register(firstUser);
        userService.register(secondUser);
        List<User> users = userService.getAllUsers();

        Wallet firstWallet = walletService.create(firstUserWallet, users.get(0).getId());
        Wallet secondWallet = walletService.create(secondUserWallet, users.get(1).getId());
        walletService.deposit(firstWallet.getId(), users.get(0).getId(), BigDecimal.TEN);
        walletService.deposit(secondWallet.getId(), users.get(1).getId(), BigDecimal.TEN);

        //when
        walletService.transferMoneyByEmail(firstWallet.getId(), BigDecimal.ONE, users.get(0), users.get(1).getEmail());

        //then
        Wallet wallet = walletService.getWalletById(firstWallet.getId());

        assertEquals(wallet.getBalance().doubleValue(), BigDecimal.valueOf(9).doubleValue());
    }
}
