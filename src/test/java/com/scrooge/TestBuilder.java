package com.scrooge;

import com.scrooge.model.AuditLog;
import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.model.enums.Country;
import com.scrooge.model.enums.Role;
import com.scrooge.web.dto.UserCreateRequest;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("wallet")
                .balance(BigDecimal.TEN)
                .currency(Currency.getInstance("USD"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Wallet secondWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("secondWallet")
                .balance(BigDecimal.TEN)
                .currency(Currency.getInstance("USD"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Pocket pocket = Pocket.builder()
                .id(UUID.randomUUID())
                .name("pocket")
                .goalDescription("test")
                .targetAmount(BigDecimal.TEN)
                .currency(Currency.getInstance("USD"))
                .balance(BigDecimal.TEN)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("sdasd")
                .lastName("123123")
                .email("demo@abv.bg")
                .password("123123")
                .phoneNumber("25423525")
                .role(Role.USER)
                .active(true)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallets(List.of(wallet, secondWallet))
                .pockets(List.of(pocket))
                .build();



        return user;
    }

    public static UserCreateRequest aRandomUserCreateRequest() {

        return  UserCreateRequest.builder()
                .email("test@emai.com")
                .password("123123")
                .confirmPassword("123123")
                .build();
    }
}
