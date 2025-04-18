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


    @PutMapping("/{id}/main-state")
    public String setMainWallet(@PathVariable(name = "id") UUID id, @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        walletService.setMainWallet(id, currentPrinciple.getId());

        return "redirect:/wallets/" + id;
    }

    @DeleteMapping("/{id}")
    public String deleteWallet(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, @PathVariable(name = "id") UUID id, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        walletService.deleteWallet(id, user);

        model.addAttribute("user", user);
        model.addAttribute("wallets", user.getWallets());

        return "redirect:/wallets";
    }

    @GetMapping("/transfer")
    public ModelAndView getTransferPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/{id}/deposit")
    public String depositToWallet(@PathVariable("id") UUID walletId,
                                  @AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                  @RequestParam("amount") BigDecimal amount,
                                  Model model) {

        Wallet wallet = walletService.deposit(walletId, currentPrinciple.getId(), amount);

        model.addAttribute("wallet", wallet);

        return "redirect:/wallets";
    }

    @PostMapping("/{id}/withdrawal")
    public String withdrawalBetweenWallets(@PathVariable(name = "id") UUID walletId,
                                                 @RequestParam("recipientWalletId") UUID recipientWalletId,
                                                 @RequestParam("amount") BigDecimal amount,
                                                 @AuthenticationPrincipal CurrentPrinciple currentPrinciple,  Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        Wallet wallet = walletService.getWalletById(walletId);
        Wallet recipient = walletService.getWalletById(recipientWalletId);

        walletService.transferMoneyBetweenWallets(walletId, recipientWalletId, amount);

        model.addAttribute("user", user);
        model.addAttribute("wallet", wallet);
        model.addAttribute("recipientWallet", recipient);

        return "redirect:/wallets";
    }

    @PutMapping("/transfer")
    public String transferMoneyByEmail(@AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                        @RequestParam BigDecimal amount,
                                        @RequestParam UUID walletId,
                                        @RequestParam String receiverEmail,
                                        Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        model.addAttribute("user", user);

        walletService.transferMoneyByEmail(walletId, amount, user, receiverEmail);

        return "redirect:/wallets/transfer";
    }

    @PutMapping("/transfer/pocket")
    public String transferMoneyToPocket(@AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                       @RequestParam BigDecimal amount,
                                       @RequestParam UUID walletId,
                                       @RequestParam UUID pocketId,
                                       Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        model.addAttribute("user", user);

        walletService.transferMoneyToPocket(walletId, amount, user, pocketId);

        return "redirect:/pockets/" + pocketId;
    }
}
