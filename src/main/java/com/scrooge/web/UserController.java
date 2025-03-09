package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ModelAndView getUser(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, UUID userId) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("account-settings");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/account-settings")
    public ModelAndView getAccountSettingsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("account-settings");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userUpdateRequest", new UserUpdateRequest());

        return modelAndView;
    }
    @PostMapping("/{id}/account-settings")
    public ModelAndView updateUser(@PathVariable UUID id, @Valid UserUpdateRequest userUpdateRequest, BindingResult bindingResult) {

        User updatedUser = userService.update(id, userUpdateRequest);

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
}
