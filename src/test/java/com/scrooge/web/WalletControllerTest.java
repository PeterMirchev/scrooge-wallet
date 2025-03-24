package com.scrooge.web;

import com.scrooge.TestBuilder;
import com.scrooge.exception.ResourceAlreadyExistException;
import com.scrooge.model.User;

import com.scrooge.model.Wallet;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.WalletCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @MockitoBean
    private WalletService walletService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWalletsPage_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        UUID userId = user.getId();

        CurrentPrinciple principal = new CurrentPrinciple(userId, user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(userId)).thenReturn(user);
        when(walletService.getAllWalletsByUserId(userId)).thenReturn(user.getWallets());

        MockHttpServletRequestBuilder request = get("/wallets")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("wallets"))
                .andExpect(model().attributeExists("wallets"));

        verify(userService, times(1)).getUserById(userId);
        verify(walletService, times(1)).getAllWalletsByUserId(userId);
    }

    @Test
    void getCreateWalletPage_happyPath() throws Exception {
        User user = TestBuilder.aRandomUser();

        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = get("/wallets/new-wallet")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("create-wallet"))
                .andExpect(model().attributeExists("walletCreateRequest"));
    }

    @Test
    void createWallet_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = post("/wallets")
                .formField("name", "wallet")
                .formField("currency", "USD")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets"));

        verify(walletService, times(1)).create(any(),any());
    }
    @Test
    void createWallet_thenThrowResourceAlreadyExistException() throws Exception {
        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        WalletCreateRequest walletCreateRequest = WalletCreateRequest.builder()
                .name("wallet")
                .currency("USD")
                .build();

        when(walletService.create(walletCreateRequest, principal.getId()))
                .thenThrow(new ResourceAlreadyExistException("Wallet name [%s] already exists".formatted("wallet")));

        MockHttpServletRequestBuilder request = post("/wallets")
                .param("name", walletCreateRequest.getName())
                .param("currency", walletCreateRequest.getCurrency())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/new-wallet"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(walletService, times(1)).create(walletCreateRequest, principal.getId());
    }

    @Test
    void createWallet_withValidationErrors() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = post("/wallets")
                .param("name", "")
                .param("currency", "USD")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("create-wallet"))
                .andExpect(model().attributeExists("walletCreateRequest"));

        verify(walletService, times(0)).create(any(), any());
    }

    @Test
    void getWalletPage_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        Wallet wallet = user.getWallets().get(0);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(walletService.getWalletById(wallet.getId())).thenReturn(wallet);

        MockHttpServletRequestBuilder request = get("/wallets/{id}", wallet.getId())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"))
                .andExpect(model().attribute("wallet", wallet))
                .andExpect(model().attribute("user", user));

        verify(userService, times(1)).getUserById(any());
        verify(walletService, times(1)).getWalletById(any());
    }
}