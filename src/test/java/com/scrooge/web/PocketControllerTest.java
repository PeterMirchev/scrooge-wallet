package com.scrooge.web;

import com.scrooge.TestBuilder;
import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.PocketService;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PocketController.class)
class PocketControllerTest {

    @MockitoBean
    private  PocketService pocketService;
    @MockitoBean
    private  UserService userService;
    @MockitoBean
    private  WalletService walletService;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void getPocketsPage_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principle = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword() ,user.getRole(), true);

        when(userService.getUserById(principle.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = get("/pockets")
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("pockets"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getUserById(any());
    }
    @Test
    void getCreatePocketPage_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principle = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword() ,user.getRole(), true);

        MockHttpServletRequestBuilder request = get("/pockets/new-pocket")
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("create-pocket"))
                .andExpect(model().attributeExists("pocketCreateRequest"));
    }

    @Test
    void createPocket_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        CurrentPrinciple principle = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword() ,user.getRole(), true);

        MockHttpServletRequestBuilder request = post("/pockets")
                .formField("name", "name")
                .formField("goalDescription", "goalDescription")
                .formField("targetAmount", "223")
                .formField("currency", "USD")
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pockets"));
        verify(pocketService, times(1)).create(any(), any());
    }

    @Test
    void getPocketPage_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        Pocket pocket = user.getPockets().get(0);
        CurrentPrinciple principle = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword() ,user.getRole(), true);

        when(userService.getUserById(principle.getId())).thenReturn(user);
        when(pocketService.getPocketById(pocket.getId())).thenReturn(pocket);

        MockHttpServletRequestBuilder request = get("/pockets/{id}", pocket.getId())
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("pocket"))
                .andExpect(model().attributeExists("pocket"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getUserById(any());
        verify(pocketService, times(1)).getPocketById(any());
    }

    @Test
    void withdrawMoneyFromPocket_happyPath() throws Exception {

        User user = TestBuilder.aRandomUser();
        Pocket pocket = user.getPockets().get(0);
        Wallet wallet = user.getWallets().get(0);
        CurrentPrinciple principle = new CurrentPrinciple(user.getId(), user.getEmail(), user.getPassword() ,user.getRole(), true);

        MockHttpServletRequestBuilder request = put("/pockets/{id}/withdrawal", pocket.getId())
                .param("walletId", wallet.getId().toString())
                .with(user(principle))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pockets"));
        verify(walletService, times(1)).depositFromPocket(any(), any(), any());
    }
}