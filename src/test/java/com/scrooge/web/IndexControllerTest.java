package com.scrooge.web;


import com.scrooge.TestBuilder;
import com.scrooge.exception.EmailAlreadyExistException;
import com.scrooge.model.enums.Role;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.UserCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;

    @Test
    void getRegisterPageThenShouldReturnRegisterPage() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userCreateRequest"));
    }

    @Test
    void getUnauthenticatedRequestToHome() throws Exception {

        when(userService.getUserById(any())).thenReturn(TestBuilder.aRandomUser());

        MockHttpServletRequestBuilder request = get("/home")
                .with(user(new CurrentPrinciple(UUID.randomUUID(),
                        "email@abv.bg",
                        "123123",
                        Role.USER,
                        true)));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void postRequestToRegister_HappyPath() throws Exception {


        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "demodemo@email.com")
                .formField("password", "123123")
                .formField("confirmPassword", "123123")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

    }

    @Test

    void postRequestToRegister_thenThrowEmailAlreadyExistException() throws Exception {

        CurrentPrinciple user = new CurrentPrinciple(UUID.randomUUID(), "test@test.abv", "123123", Role.USER, true);

        doThrow(new EmailAlreadyExistException("Email already registered - [demodemo@email.com]"))
                .when(userService).register(any(UserCreateRequest.class));

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "demodemo@email.com")
                .formField("password", "123123")
                .formField("confirmPassword", "123123")
                .with(csrf()).with(user(user));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/register"))
                .andExpect(flash().attributeExists("emailAlreadyExistException"))
                .andExpect(flash().attributeCount(1));

        verify(userService, times(1)).register(any(UserCreateRequest.class));
    }

    @Test
    void getLoginPage_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void getLoginPage_authenticatedUser_redirectsToHome() throws Exception {

        CurrentPrinciple user = new CurrentPrinciple(UUID.randomUUID(), "test@test.abv", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/login")
                .with(user(user));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void getIndexPage_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}