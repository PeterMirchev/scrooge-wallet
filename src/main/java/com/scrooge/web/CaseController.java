package com.scrooge.web;

import com.scrooge.event.dto.CaseMessageRequest;
import com.scrooge.event.dto.MessageCache;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.CaseCacheService;
import com.scrooge.service.MessageCacheService;
import com.scrooge.service.UserService;
import com.scrooge.event.dto.CaseCache;
import com.scrooge.event.dto.CaseUpdateRequest;
import com.scrooge.web.mapper.CaseCacheMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("cases")
public class CaseController {

    private final UserService userService;
    private final CaseCacheService caseCacheService;
    private final MessageCacheService messageCacheService;

    public CaseController(UserService userService, CaseCacheService caseCacheService, MessageCacheService messageCacheService) {
        this.userService = userService;
        this.caseCacheService = caseCacheService;
        this.messageCacheService = messageCacheService;
    }

    @GetMapping()
    public String getCasesPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());
        List<CaseCache> cases = caseCacheService.getAllCases();
        List<MessageCache> messages = messageCacheService.getAllMessages();

        model.addAttribute("user", user);
        model.addAttribute("cases", cases);
        model.addAttribute("messages", messages);

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

        List<MessageCache> messages = messageCacheService.getAllMessagesByCase(id);

        CaseUpdateRequest updateRequest = CaseCacheMapper.toUpdateRequest(selectedCase);
        CaseMessageRequest messageRequest = CaseCacheMapper.toMessageRequest(user);

        model.addAttribute("case", selectedCase);
        model.addAttribute("user", user);
        model.addAttribute("caseUpdateRequest", updateRequest);
        model.addAttribute("caseMessageRequest", messageRequest);
        model.addAttribute("messages", messages);

        return "case-detail";
    }

    @PutMapping("/{caseId}/case-details")
    public String updateCase(@PathVariable UUID caseId, @ModelAttribute CaseUpdateRequest caseUpdateRequest,
                             @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());

        caseCacheService.updateCase(caseId, user, caseUpdateRequest);

        return "redirect:/cases/" + caseId;
    }

    @PostMapping("/{caseId}/message")
    public String caseMessageUpdate(@PathVariable UUID caseId, @ModelAttribute CaseMessageRequest messageRequest,
                             @AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());

        messageCacheService.sendMessage(caseId, user, messageRequest);

        return "redirect:/cases/" + caseId;
    }
}
