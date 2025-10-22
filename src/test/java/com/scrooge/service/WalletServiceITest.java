package com.scrooge.service;

import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.repository.AuditLogRepository;
import com.scrooge.repository.UserRepository;
import com.scrooge.repository.WalletRepository;
import com.scrooge.web.dto.pocket.PocketCreateRequest;
import com.scrooge.web.dto.user.UserCreateRequest;
import com.scrooge.web.dto.wallet.WalletCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    private PocketService pocketService;

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

    @Test
    void whenTransferMoneyBetweenWallets_thenHappyPath() {

        //given
        UserCreateRequest user = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();

        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstWallet")
                .currency("EUR")
                .build();

        WalletCreateRequest secondUserWallet = WalletCreateRequest.builder()
                .name("secondWallet")
                .currency("EUR")
                .build();

        userService.register(user);
        List<User> users = userService.getAllUsers();

        Wallet firstWallet = walletService.create(firstUserWallet, users.get(0).getId());
        Wallet secondWallet = walletService.create(secondUserWallet, users.get(0).getId());
        walletService.deposit(firstWallet.getId(), users.get(0).getId(), BigDecimal.TEN);

        //when
        walletService.transferMoneyBetweenWallets(firstWallet.getId(), secondWallet.getId(), BigDecimal.TEN);

        //then
        Wallet wallet = walletService.getWalletById(firstWallet.getId());

        assertEquals(wallet.getBalance().doubleValue(), BigDecimal.ZERO.doubleValue());
    }

    @Test
    void whenTransferMoneyToPocket_thenHappyPath() {

        //given
        UserCreateRequest user = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();
        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();
        PocketCreateRequest pocketCreateRequest = PocketCreateRequest.builder()
                .currency(Currency.getInstance("EUR"))
                .goalDescription("Test")
                .name("Test")
                .targetAmount(BigDecimal.TEN)
                .build();

        userService.register(user);
        List<User> users = userService.getAllUsers();
        Wallet wallet = walletService.create(firstUserWallet, users.get(0).getId());
        walletService.deposit(wallet.getId(), users.get(0).getId(), BigDecimal.TEN);
        Pocket pocket = pocketService.create(pocketCreateRequest, users.get(0).getId());

        //when
        walletService.transferMoneyToPocket(wallet.getId(), BigDecimal.TEN, users.get(0), pocket.getId());

        //then
        assertEquals(wallet.getBalance().doubleValue(), BigDecimal.ZERO.doubleValue());
    }

    @Test
    void whenDepositFromPocket_thenHappyPath() {

        //given
        UserCreateRequest user = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();
        userService.register(user);

        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();

        PocketCreateRequest pocketCreateRequest = PocketCreateRequest.builder()
                .currency(Currency.getInstance("EUR"))
                .goalDescription("Test")
                .name("Test")
                .targetAmount(BigDecimal.TEN)
                .build();

        List<User> users = userService.getAllUsers();
        Wallet wallet = walletService.create(firstUserWallet, users.get(0).getId());
        Pocket pocket = pocketService.create(pocketCreateRequest, users.get(0).getId());
        walletService.deposit(wallet.getId(), users.get(0).getId(), BigDecimal.TEN);
        pocketService.deposit(BigDecimal.TEN, pocket.getId(), users.get(0).getId());

        //when
        walletService.depositFromPocket(wallet.getId(), pocket.getId(), users.get(0).getId());

        //then
        assertEquals(users.get(0).getPockets().size(), 0);
    }

    @Test
    void whenDeleteWallet_thenHappyPath() {

        //given
        UserCreateRequest user = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();
        userService.register(user);

        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();
        WalletCreateRequest secondUserWallet = WalletCreateRequest.builder()
                .name("secondWallet")
                .currency("EUR")
                .build();

        List<User> users = userService.getAllUsers();
        Wallet wallet = walletService.create(firstUserWallet, users.get(0).getId());
        Wallet secondWallet = walletService.create(secondUserWallet, users.get(0).getId());
        wallet.setMainWallet(true);

        users.get(0).getWallets().add(wallet);
        users.get(0).getWallets().add(secondWallet);

        //when
        walletService.deleteWallet(wallet.getId(), users.get(0));

        //then
        assertEquals(users.get(0).getWallets().size(),1);
        assertTrue(secondWallet.isMainWallet());
    }

    @Test
    void whenSetMainWallet_thenHappyPath() {

        //given
        UserCreateRequest user = UserCreateRequest.builder()
                .email("firstUser@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();
        userService.register(user);

        WalletCreateRequest firstUserWallet = WalletCreateRequest.builder()
                .name("firstUser")
                .currency("EUR")
                .build();
        WalletCreateRequest secondUserWallet = WalletCreateRequest.builder()
                .name("secondWallet")
                .currency("EUR")
                .build();

        List<User> users = userService.getAllUsers();
        Wallet wallet = walletService.create(firstUserWallet, users.get(0).getId());
        Wallet secondWallet = walletService.create(secondUserWallet, users.get(0).getId());
        wallet.setMainWallet(true);

        users.get(0).getWallets().add(wallet);
        users.get(0).getWallets().add(secondWallet);
        wallet.setMainWallet(false);
        secondWallet.setMainWallet(true);

        //when
        walletService.setMainWallet(secondWallet.getId(), users.get(0).getId());

        //then
        assertTrue(secondWallet.isMainWallet());
        assertFalse(wallet.isMainWallet());
    }
}
