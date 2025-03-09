package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.PocketService;
import com.scrooge.service.UserService;
import com.scrooge.service.WalletService;
import com.scrooge.web.dto.PocketCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

}
