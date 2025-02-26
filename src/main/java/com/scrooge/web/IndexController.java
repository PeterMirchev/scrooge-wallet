package com.scrooge.web;

import com.scrooge.model.User;
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

        if (currentPrinciple != null) {
            return "redirect:/home";
        }

        return "index";
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/pockets")
    public ModelAndView getPocketsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pockets");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/audit-logs")
    public ModelAndView getAuditLogPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("audit-logs");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/transactions")
    public ModelAndView getTransactionsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
 @GetMapping("/transfer")
    public ModelAndView getTransferPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
 @GetMapping("/account-settings")
    public ModelAndView getAccountSettingsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("account-settings");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

}
