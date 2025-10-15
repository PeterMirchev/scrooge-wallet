package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.SupportService;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.SupportRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final UserService userService;
    private final SupportService supportService;

    public SupportController(UserService userService, SupportService supportService) {
        this.userService = userService;
        this.supportService = supportService;
    }

    @GetMapping
    public String getSupportPage(SupportRequest request,
                                 @AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                 Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        model.addAttribute("user", user);
        model.addAttribute("supportRequest", request);

        return "contact-support";
    }

    @PostMapping
    public String sendSupportMessage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                                     @Valid @ModelAttribute SupportRequest supportRequest,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("supportRequest", supportRequest);
            return "contact-support";
        }

        User user = userService.getUserById(currentPrinciple.getId());

        supportService.sendMessageToSupport(user, supportRequest);

        redirectAttributes.addFlashAttribute("successMessage",
                "Your message has been sent! Our support team will contact you shortly.");

        return "redirect:/home";
    }
}
