package com.scrooge.web;

import com.scrooge.model.AuditLog;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.AuditLogService;
import com.scrooge.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserService userService;

    public AuditLogController(AuditLogService auditLogService, UserService userService) {
        this.auditLogService = auditLogService;
        this.userService = userService;
    }

    @GetMapping("/audit-logs")
    public String getAuditLogs(@AuthenticationPrincipal CurrentPrinciple currentPrinciple, Model model) {

        User user = userService.getUserById(currentPrinciple.getId());
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForUser(user.getId(), 3);

        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("user", user);

        return "home";
    }
}
