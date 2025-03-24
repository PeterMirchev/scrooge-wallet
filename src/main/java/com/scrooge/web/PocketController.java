package com.scrooge.web;

import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.PocketService;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.PocketCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/pockets")
public class PocketController {

    private final PocketService pocketService;
    private final UserService userService;
    private final WalletService walletService;

    public PocketController(PocketService pocketService, UserService userService, WalletService walletService) {
        this.pocketService = pocketService;
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping()
    public ModelAndView getPocketsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pockets");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/new-pocket")
    public ModelAndView getCreatePocketPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("create-pocket");
        modelAndView.addObject("pocketCreateRequest", new PocketCreateRequest());

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView createPocket(@Valid @ModelAttribute PocketCreateRequest pocketCreateRequest,
                                     @AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                     BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("create-pocket");
            modelAndView.addObject("pocketCreateRequest", pocketCreateRequest);
            return modelAndView;
        }

        pocketService.create(pocketCreateRequest, currentPrinciple.getId());
        modelAndView.setViewName("redirect:/pockets");
        return modelAndView;
    }

    @GetMapping("/{id}")
    public String getPocketPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, @PathVariable(name = "id") UUID id, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        Pocket pocket = pocketService.getPocketById(id);
        model.addAttribute("pocket", pocket);
        model.addAttribute("user", user);

        return "pocket";
    }

    @PutMapping("/{id}/withdrawal")
    public String withdrawMoneyFromPocket(@PathVariable(name = "id") UUID id, @AuthenticationPrincipal CurrentPrinciple currentPrinciple, @RequestParam UUID walletId) {

        walletService.depositFromPocket(walletId, id, currentPrinciple.getId());

        return "redirect:/pockets";
    }
}
