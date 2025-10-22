package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.notification.NotificationPreferenceResponse;
import com.scrooge.web.dto.user.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ModelAndView getAllUsers(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        List<User> users = userService.getAllUsers();

        User admin = userService.getUserById(currentPrinciple.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("admin", admin);

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String switchUserStatus(@PathVariable UUID id) {

        userService.switchStatus(id);

        return "redirect:/users";
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String switchUserRole(@PathVariable UUID id) {

        userService.switchRole(id);

        return "redirect:/users";
    }

    @GetMapping("/account-settings")
    public ModelAndView getAccountSettingsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        NotificationPreferenceResponse notificationPreferenceResponse = userService.getNotificationPreference(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("account-settings");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userUpdateRequest", new UserUpdateRequest());
        modelAndView.addObject("notificationPreference", notificationPreferenceResponse);

        return modelAndView;
    }

    @PutMapping("/{id}/account-settings")
    public ModelAndView updateUser(@PathVariable UUID id, @Valid UserUpdateRequest userUpdateRequest, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            User user = userService.getUserById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("account-settings");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userUpdateRequest", userUpdateRequest);
        }

        userService.update(id, userUpdateRequest);
        return new ModelAndView("redirect:/home");
    }

    @PutMapping("/{id}/account-settings/preference")
    public String switchNotificationPreference(@PathVariable(name = "id") UUID uuid) {

        userService.switchNotificationPreference(uuid);

        return "redirect:/users/account-settings";
    }
}
