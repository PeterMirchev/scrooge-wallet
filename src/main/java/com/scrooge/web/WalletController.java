package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.WalletCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    @GetMapping("/new-wallet")
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

    @GetMapping("/{id}")
    public String getWalletPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, Model model, @PathVariable(name = "id") UUID id) {

        User user = userService.getUserById(currentPrinciple.getId());

        Wallet wallet = walletService.getWalletById(id);
        model.addAttribute("wallet", wallet);
        model.addAttribute("user", user);

        return "wallet";
    }

    @PostMapping("/{id}/deposit")
    public String depositToWallet(@PathVariable("id") UUID walletId,
                                  @AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                  @RequestParam("amount") BigDecimal amount,
                                  Model model) {

        Wallet wallet = walletService.deposit(walletId, currentPrinciple.getId(), amount);

        model.addAttribute("wallet", wallet);

        return "redirect:/wallets/{id}";
    }

    @GetMapping("/{id}/withdrawal")
    public String getTransferForm(@PathVariable("id") UUID id,
                                   @AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                   Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        Wallet wallet = walletService.getWalletById(id);

        List<Wallet> wallets = walletService.getAllWalletsByUserId(user.getId());
        model.addAttribute("currentWallet", wallet);
        model.addAttribute("wallets", wallets);

        return "wallet";
    }


    @PostMapping("/{walletId}/withdrawal")
    public ModelAndView withdrawalBetweenWallets(@PathVariable(name = "walletId") UUID walletId,
                                                 @RequestParam("recipientWalletId") UUID recipientWalletId,
                                                 @RequestParam("amount") BigDecimal amount,
                                                 @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        ModelAndView modelAndView = new ModelAndView();

        User user = userService.getUserById(currentPrinciple.getId());

        Wallet wallet = walletService.getWalletById(walletId);
        Wallet recipient = walletService.getWalletById(recipientWalletId);

        walletService.transferMoneyBetweenWallets(walletId, recipientWalletId, amount);

        modelAndView.setViewName("wallet");
        modelAndView.addObject("user", user);
        modelAndView.addObject("wallet", wallet);
        modelAndView.addObject("recipientWallet", recipient);

        return modelAndView;
    }

    @PutMapping("/{id}/main-state")
    public String setMainWallet(@PathVariable(name = "id") UUID id, @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {


        walletService.setMainWallet(id, currentPrinciple.getId());

        return "redirect:/wallets/" + id;
    }

}
