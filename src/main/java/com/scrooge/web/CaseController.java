package com.scrooge.web;

import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.CaseCacheService;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.cases.CaseCache;
import com.scrooge.web.dto.cases.CaseUpdateRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("cases")
public class CaseController {

    private final UserService userService;
    private final CaseCacheService caseCacheService;

    public CaseController(UserService userService, CaseCacheService caseCacheService) {
        this.userService = userService;
        this.caseCacheService = caseCacheService;
    }

    @GetMapping()
    public String getCasesPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());
        List<CaseCache> cases = caseCacheService.getAllCases();

        model.addAttribute("user", user);
        model.addAttribute("cases", cases);

        return "cases";
    }

    @GetMapping("/{id}")
    public String caseDetail(@AuthenticationPrincipal CurrentPrinciple currentPrinciple,
                             @PathVariable UUID id, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());

        var selectedCase = caseCacheService.getCase(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("case", selectedCase);
        model.addAttribute("user", user);
        model.addAttribute("caseUpdateRequest", new CaseUpdateRequest());

        return "case-detail";
    }
}
