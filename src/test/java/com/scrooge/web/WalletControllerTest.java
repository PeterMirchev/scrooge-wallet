package com.scrooge.web;

import com.scrooge.TestBuilder;
import com.scrooge.exception.*;
import com.scrooge.model.User;

import com.scrooge.model.Wallet;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.wallet.WalletCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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


    @Test
    void depositToWallet_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        Wallet  wallet = user.getWallets().get(0);

        when(walletService.getWalletById(wallet.getId())).thenReturn(wallet);
        when(userService.getUserById(principal.getId())).thenReturn(user);
        when(walletService.deposit(wallet.getId(), principal.getId(), BigDecimal.TEN)).thenReturn(wallet);

        MockHttpServletRequestBuilder request = post("/wallets/{id}/deposit", wallet.getId())
                .param("amount", BigDecimal.TEN.toString())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets"));

        verify(walletService, times(1)).deposit(any(), any(), any());
    }

    @Test
    void depositToWallet_thenThrowInvalidInternalTransferAmountException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        Wallet  wallet = user.getWallets().get(0);

        when(walletService.getWalletById(wallet.getId())).thenReturn(wallet);
        when(userService.getUserById(principal.getId())).thenReturn(user);
        when(walletService.deposit(wallet.getId(), principal.getId(), BigDecimal.TEN)).thenThrow(new InvalidInternalTransferAmountException(AMOUNT_MUST_BE_GREATER_THAN_ZERO));

        MockHttpServletRequestBuilder request = post("/wallets/{id}/deposit", wallet.getId())
                .param("amount", BigDecimal.TEN.toString())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/" + wallet.getId()))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(walletService, times(1)).deposit(any(), any(), any());
    }

    @Test
    void withdrawalBetweenWallets_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        Wallet  wallet = user.getWallets().get(0);
        Wallet recipient = user.getWallets().get(1);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        when(walletService.getWalletById(wallet.getId())).thenReturn(wallet);
        when(walletService.getWalletById(recipient.getId())).thenReturn(recipient);

        MockHttpServletRequestBuilder request = post("/wallets/{id}/withdrawal", wallet.getId())
                .param("recipientWalletId", recipient.getId().toString())
                .param("amount", BigDecimal.TEN.toString())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets"));

        verify(userService, times(1)).getUserById(any());
        verify(walletService, times(2)).getWalletById(any());
        verify(walletService, times(1)).transferMoneyBetweenWallets(any(), any(), any());
    }

    @Test
    void deleteWallet_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        Wallet wallet = user.getWallets().get(0);

        when(userService.getUserById(principal.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = delete("/wallets/{id}", wallet.getId())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets"));

        verify(walletService, times(1)).deleteWallet(any(), any());
        verify(userService, times(1)).getUserById(any());
    }

    @Test
    void deleteWallet_throwWalletAmountMustBeZeroException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        Wallet wallet = user.getWallets().get(0);

        doThrow(new WalletAmountMustBeZeroException(WALLET_AMOUNT_MUST_BE_ZERO))
                .when(walletService).deleteWallet(any(), any());

        MockHttpServletRequestBuilder request = delete("/wallets/{id}", wallet.getId())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/" + wallet.getId()));

        verify(walletService, times(1)).deleteWallet(any(), any());
        verify(userService, times(1)).getUserById(any());
    }

    @Test
    void getTransferPage_happyPage() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = get("/wallets/transfer")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getUserById(any());
    }

    @Test
    void transferMoneyByEmail_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = put("/wallets/transfer")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("receiverEmail", "test@test.com")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/transfer"));

        verify(walletService, times(1)).transferMoneyByEmail(any(), any(), any(), any());
    }

    @Test
    void transferMoneyToPocket_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);

        UUID pocketId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/wallets/transfer/pocket")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("pocketId", pocketId.toString())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pockets/" + pocketId));

        verify(walletService, times(1)).transferMoneyToPocket(any(), any(), any(), any());
    }

    @Test
    void transferMoneyByEmail_thenInvalidUserEmailException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        doThrow(new InvalidUserEmailException(INVALID_USER_EMAIL.formatted("invalid@email.com")))
                .when(walletService)
                .transferMoneyByEmail(any(), any(), any(), any());

        MockHttpServletRequestBuilder request = put("/wallets/transfer")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("receiverEmail", "invalid@email.com")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("invalidUserEmailException"))
                .andExpect(redirectedUrl("/wallets/transfer"));

        verify(walletService, times(1)).transferMoneyByEmail(any(), any(), any(), any());
    }

    @Test
    void transferMoneyByEmail_thenReceiverHasNoWalletException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        doThrow(new ReceiverHasNoWalletException("%s cannot accept money right now.".formatted("invalid@email.com")))
                .when(walletService)
                .transferMoneyByEmail(any(), any(), any(), any());

        MockHttpServletRequestBuilder request = put("/wallets/transfer")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("receiverEmail", "invalid@email.com")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("receiverHasNoWalletException"))
                .andExpect(redirectedUrl("/wallets/transfer"));

        verify(walletService, times(1)).transferMoneyByEmail(any(), any(), any(), any());
    }

    @Test
    void transferMoneyByEmail_thenInsufficientTransferAmountException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        doThrow(new InsufficientTransferAmountException("%s cannot accept money right now.".formatted("invalid@email.com")))
                .when(walletService)
                .transferMoneyByEmail(any(), any(), any(), any());

        MockHttpServletRequestBuilder request = put("/wallets/transfer")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("receiverEmail", "invalid@email.com")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("insufficientTransferAmountException"))
                .andExpect(redirectedUrl("/wallets/transfer"));

        verify(walletService, times(1)).transferMoneyByEmail(any(), any(), any(), any());
    }

    @Test
    void transferMoneyByEmail_thenInsufficientAmountException() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);

        when(userService.getUserById(principal.getId())).thenReturn(user);
        doThrow(new InsufficientAmountException("%s cannot accept money right now.".formatted("invalid@email.com")))
                .when(walletService)
                .transferMoneyByEmail(any(), any(), any(), any());

        MockHttpServletRequestBuilder request = put("/wallets/transfer")
                .param("amount", BigDecimal.TEN.toString())
                .param("walletId", UUID.randomUUID().toString())
                .param("receiverEmail", "invalid@email.com")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(redirectedUrl("/wallets/transfer"));

        verify(walletService, times(1)).transferMoneyByEmail(any(), any(), any(), any());
    }

    @Test
    void setMainWallet_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principal = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), true);
        UUID walletId = user.getWallets().get(0).getId();
        MockHttpServletRequestBuilder request = put("/wallets/{id}/main-state", walletId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/" + walletId));

        verify(walletService, times(1)).setMainWallet(any(), any());
    }

}