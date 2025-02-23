package com.scrooge.web;

import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.RequestLogin;
import com.scrooge.web.dto.UserCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("requestLogin", new RequestLogin());

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("userCreateRequest", new UserCreateRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public String register(@Valid UserCreateRequest userCreateRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(userCreateRequest);

        return "redirect:/login";
    }

    @GetMapping()
    public String getIndexPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        System.out.println();
        return "index";
    }

}
