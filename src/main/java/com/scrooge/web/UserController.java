package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ModelAndView getUser(UUID userId) {

        User user = userService.getUserById(userId);
        ModelAndView modelAndView = new ModelAndView();

       return null;
    }
}
