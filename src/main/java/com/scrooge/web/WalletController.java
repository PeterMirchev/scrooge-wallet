package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.WalletCreateRequest;
import jakarta.validation.Valid;
import org.springframework.boot.Banner;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/wallets")
public class WalletController {
    private final WalletService walletService;
    private final UserService userService;

    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public String getWalletsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        List<Wallet> wallets = walletService.getAllWalletsByUserId(user.getId());
        model.addAttribute("wallets", wallets);

        return "wallets";
    }

    @GetMapping("/new")
    public ModelAndView getCreateWalletPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("create-wallet");
        modelAndView.addObject("walletCreateRequest", new WalletCreateRequest());

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView createWallet(@Valid @ModelAttribute WalletCreateRequest walletCreateRequest,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("create-wallet");
            modelAndView.addObject("walletCreateRequest", walletCreateRequest);
            return modelAndView;
        }

        walletService.create(walletCreateRequest, currentPrinciple.getId());
        modelAndView.setViewName("redirect:/wallets");

        return modelAndView;
    }
}
