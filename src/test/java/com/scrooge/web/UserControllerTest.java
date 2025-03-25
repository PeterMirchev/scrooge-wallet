package com.scrooge.web;

import com.scrooge.TestBuilder;
import com.scrooge.exception.EmailAlreadyExistException;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.model.enums.Country;
import com.scrooge.model.enums.Role;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.NotificationPreferenceResponse;
import com.scrooge.web.dto.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {

        CurrentPrinciple principal = new CurrentPrinciple(UUID.randomUUID(), "User123", "123123", Role.ADMINISTRATOR, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).switchRole(any());
    }

    @Test
    void putUnauthorizedRequestToSwitchRole_shouldReturn404AndNotFoundView() throws Exception {

        UUID userId = UUID.randomUUID();

        CurrentPrinciple principal = new CurrentPrinciple(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
    }

    @Test
    void getAllUsers_happyPath() throws Exception {

        UUID adminId = UUID.randomUUID();
        CurrentPrinciple principal = new CurrentPrinciple(adminId, "adminUser", "password", Role.ADMINISTRATOR, true);

        User adminUser = User.builder()
                .id(adminId)
                .firstName("adminUser")
                .email("admin@example.com")
                .role(Role.ADMINISTRATOR)
                .active(true)
                .build();

        User user1 = TestBuilder.aRandomUser();
        user1.setCountry(Country.UNITED_STATES);
        User user2 = TestBuilder.aRandomUser();
        user2.setCountry(null);
        List<User> users = List.of(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userService.getUserById(adminId)).thenReturn(adminUser);

        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", users))
                .andExpect(model().attributeExists("admin"))
                .andExpect(model().attribute("admin", adminUser));

        verify(userService, times(1)).getAllUsers();
        verify(userService, times(1)).getUserById(adminId);
    }

    @Test
    void handleResourceNotFoundException() throws Exception {
        UUID adminId = UUID.randomUUID();
        CurrentPrinciple principal = new CurrentPrinciple(adminId, "adminUser", "password", Role.ADMINISTRATOR, true);

        when(userService.getAllUsers()).thenThrow(new ResourceNotFoundException("User not found"));

        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wallets/new-wallet"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void handleEmailAlreadyExistException() throws Exception {

        CurrentPrinciple principal = new CurrentPrinciple(UUID.randomUUID(), "adminUser@email.com", "password", Role.ADMINISTRATOR, true);
        when(userService.getAllUsers()).thenThrow(new EmailAlreadyExistException("Email already exists"));

        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("emailAlreadyExistException"));
    }

    @Test
    void getAccountSettingsPage_happyPath() throws Exception {

        UUID adminId = UUID.randomUUID();
        CurrentPrinciple principal = new CurrentPrinciple(adminId, "adminUser@email.com", "password", Role.ADMINISTRATOR, true);

        User adminUser = User.builder()
                .id(adminId)
                .firstName("adminUser")
                .email("admin@example.com")
                .role(Role.ADMINISTRATOR)
                .active(true)
                .build();

        NotificationPreferenceResponse notificationPreferenceResponse = NotificationPreferenceResponse.builder()
                .id(UUID.randomUUID())
                .userId(adminId)
                .enableNotification(true)
                .email(principal.getEmail())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(userService.getUserById(principal.getId())).thenReturn(adminUser);
        when(userService.getNotificationPreference(principal.getId())).thenReturn(notificationPreferenceResponse);

        MockHttpServletRequestBuilder request = get("/users/account-settings")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("account-settings"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("notificationPreference"))
                .andExpect(model().attribute("userUpdateRequest", new UserUpdateRequest()));

        verify(userService, times(1)).getUserById(any());
        verify(userService, times(1)).getNotificationPreference(any());
    }

    @Test
    void updateUser_happyPath() throws Exception {

        UUID userId = UUID.randomUUID();
        CurrentPrinciple principal = new CurrentPrinciple(userId, "adminUser@email.com", "password", Role.ADMINISTRATOR, true);
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .firstName("UpdatedName")
                .lastName("UpdatedName")
                .build();


        User existingUser = User.builder()
                .id(userId)
                .firstName("ExistingUser")
                .email("existingUser@example.com")
                .role(Role.USER)
                .active(true)
                .build();

        when(userService.getUserById(userId)).thenReturn(existingUser);

        MockHttpServletRequestBuilder request = put("/users/{id}/account-settings", userId)
                .param("firstName", userUpdateRequest.getFirstName())
                .param("lastName", userUpdateRequest.getLastName())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).update(userId, userUpdateRequest);
    }

    @Test
    void switchNotificationPreference_success() throws Exception {

        UUID userId = UUID.randomUUID();
        CurrentPrinciple principal = new CurrentPrinciple(userId, "adminUser@email.com", "password", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/account-settings/preference", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/account-settings"));

        verify(userService, times(1)).switchNotificationPreference(userId);
    }
}