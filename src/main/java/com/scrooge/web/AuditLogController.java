package com.scrooge.web;

import com.scrooge.model.AuditLog;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.AuditLogService;
import com.scrooge.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;

    }

    @GetMapping()
    public ModelAndView getAuditLogPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        List<AuditLog> auditLogs = auditLogService.getAuditLogsForUser(currentPrinciple.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("audit-logs");
        modelAndView.addObject("auditLogs", auditLogs);

        return modelAndView;
    }
}
